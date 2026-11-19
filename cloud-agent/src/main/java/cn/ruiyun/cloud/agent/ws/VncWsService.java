package cn.ruiyun.cloud.agent.ws;

import cn.hutool.core.util.NumberUtil;
import cn.ruiyun.cloud.agent.config.WebSocketConfig;
import cn.ruiyun.cloud.agent.sock.NioCallback;
import cn.ruiyun.cloud.agent.sock.NioClient;
import cn.ruiyun.cloud.agent.sock.NioSelector;
import cn.ruiyun.cloud.agent.util.*;
import cn.ruiyun.cloud.common.bean.Graphics;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.gson.GsonBuilderUtil;
import cn.ruiyun.cloud.common.util.AppUtils;
import cn.ruiyun.cloud.common.util.ErrorCode;
import com.google.common.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.libvirt.Connect;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@ServerEndpoint(value = "/api/vnc", configurator = WebSocketConfig.class)
@Component
public class VncWsService implements NioCallback {

    private Session session;
    private NioClient vncClient;
    private ClientService clientService;
    private NioSelector nioSelector;
    private String guestName = "unknown";
    private long guestId;
    private long hostId;
    private long clusterId;

    @SneakyThrows
    @OnOpen
    public void onVncConnect(Session session) {
        this.session = session;
        this.clientService = SpringContextUtils.getBean(ClientService.class);
        this.nioSelector = SpringContextUtils.getBean(NioSelector.class);
        String data = this.getRequestData();
        if (StringUtils.isBlank(data)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "VNC连接参数不能为空");
        }
        Map<String, Object> map = GsonBuilderUtil.create().fromJson(data, new TypeToken<Map<String, Object>>() {
        }.getType());
        if (map == null) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "VNC连接参数格式错误");
        }
        String name = (String) map.get("name");
        this.guestName = StringUtils.defaultIfBlank(name, "unknown");
        this.guestId = NumberUtil.parseLong(String.valueOf(map.getOrDefault("guestId", "0")));
        this.hostId = NumberUtil.parseLong(String.valueOf(map.getOrDefault("hostId", "0")));
        this.clusterId = NumberUtil.parseLong(String.valueOf(map.getOrDefault("clusterId", "0")));
        log.info("接收到VNC WebSocket连接,guestId={},guestName={},hostId={},clusterId={},sessionId={}",
                guestId, guestName, hostId, clusterId, session.getId());
        String clientId = (String) map.get("clientId");
        String nonce = (String) map.get("nonce");
        long timestamp = NumberUtil.parseLong(map.getOrDefault("timestamp", "0").toString());
        String sign = (String) map.remove("sign");

        if (StringUtils.isAnyBlank(name, clientId, nonce, sign)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "VNC连接参数不完整");
        }

        Connect connect = null;
        try {
            if (!SignatureReplayGuard.isTimestampValid(timestamp, 60000)) {
                log.error("签名错误:签名时间验证失败,请确认服务器时间是否同步");
                throw new CodeException(ErrorCode.SERVER_ERROR, "签名时间验证失败");
            }
            if (!Objects.equals(clientService.getClientId(), clientId)) {
                log.error("签名错误:当前客户端已加入其他系统，如需重新加入，请删除当前路径下config.json，重启后重新加入");
                throw new CodeException(ErrorCode.SERVER_ERROR);
            }
            String dataSign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
            if (!Objects.equals(dataSign, sign)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "签名错误:签名验证失败.");
            }
            if (!SignatureReplayGuard.useNonce("vnc-websocket", nonce, 60000)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "签名错误:nonce已使用");
            }
            log.info("开始查询虚拟机信息:{}", name);
            connect = ConnectFactory.create();
            int port = 0;
            try {
                String xml = connect.domainLookupByName(name).getXMLDesc(0);
                Graphics graphics = GraphicsUtil.getGraphics(xml);
                if (graphics == null || graphics.getPort() <= 0) {
                    throw new IllegalStateException("虚拟机未配置可用的VNC图形设备");
                }
                port = graphics.getPort();
            } catch (Exception err) {
                log.error("查询虚拟机信息信息失败.name={}", name, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
            String host = "127.0.0.1";
            log.info("获取到vnc端口:{}={}", name, port);
            long startTime = System.currentTimeMillis();
            try {
                this.vncClient = this.nioSelector.createClient(host, port, this);
                log.info("开始连接到{} vnc://{}:{} 成功 time={}", name, host, port, System.currentTimeMillis() - startTime);
            } catch (Exception err) {
                log.error("连接到{} vnc://{}:{} 失败 time={}", name, host, port, System.currentTimeMillis() - startTime, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
        } finally {
            if (connect != null) {
                connect.close();
            }
        }
    }

    private String getRequestData() {
        List<String> queryValues = session.getRequestParameterMap().get("data");
        if (queryValues != null && !queryValues.isEmpty()) {
            return queryValues.get(0);
        }
        return getHeader("x-data");
    }

    @SuppressWarnings("unchecked")
    private String getHeader(String name) {
        Map<String, List<String>> headers = (Map<String, List<String>>) session.getUserProperties().get("Header");
        if (headers == null) {
            return null;
        }
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .filter(values -> values != null && !values.isEmpty())
                .map(values -> values.get(0))
                .findFirst()
                .orElse(null);
    }

    @OnClose
    public void onVncClose() {
        log.info("VNC WebSocket连接关闭,guestId={},guestName={},hostId={},sessionId={}",
                guestId, guestName, hostId, sessionId());
        this.close();
    }

    @SneakyThrows
    @OnMessage
    public void onVncMessage(byte[] messages, Session session) {
        if (this.vncClient != null) {
            this.vncClient.send(messages);
        }
    }

    @OnError
    public void onVncError(Session session, Throwable error) {
        log.warn("VNC WebSocket连接异常,guestId={},guestName={},hostId={},clusterId={},sessionId={}",
                guestId, guestName, hostId, clusterId, session == null ? sessionId() : session.getId(), error);
        this.close();
    }

    @Synchronized
    private void close() {
        if (this.session != null) {
            try {
                this.session.close();
            } catch (Exception ignored) {

            } finally {
                this.session = null;
            }
        }
        if (this.vncClient != null) {
            try {
                this.vncClient.close();
            } catch (Exception ignored) {

            } finally {
                this.vncClient = null;
            }
        }

    }


    @Override
    public void onClose() {
        log.info("VNC TCP连接关闭,guestId={},guestName={},hostId={}", guestId, guestName, hostId);
        this.close();
    }

    @Override
    public void onError(Exception err) {
        log.warn("VNC TCP连接异常,guestId={},guestName={},hostId={},clusterId={}",
                guestId, guestName, hostId, clusterId, err);
        this.close();
    }

    @Override
    public void onData(ByteBuffer buffer) {
        try {
            Session currentSession = this.session;
            if (currentSession == null || !currentSession.isOpen()) {
                this.close();
                return;
            }
            currentSession.getBasicRemote().sendBinary(buffer);
        } catch (Exception err) {
            log.warn("VNC数据发送到浏览器失败,guestId={},guestName={},hostId={},sessionId={}",
                    guestId, guestName, hostId, sessionId(), err);
            this.close();
        }
    }

    private String sessionId() {
        return this.session == null ? "closed" : this.session.getId();
    }
}
