package cn.chenjun.cloud.agent.ws;

import cn.chenjun.cloud.agent.config.WebSocketConfig;
import cn.chenjun.cloud.agent.sock.NioCallback;
import cn.chenjun.cloud.agent.sock.NioClient;
import cn.chenjun.cloud.agent.sock.NioSelector;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.util.ConnectFactory;
import cn.chenjun.cloud.agent.util.SpringContextUtils;
import cn.chenjun.cloud.agent.util.VncUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.util.NumberUtil;
import com.google.common.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

    @SneakyThrows
    @OnOpen
    public void onVncConnect(Session session) {
        this.session = session;
        this.clientService = SpringContextUtils.getBean(ClientService.class);
        this.nioSelector = SpringContextUtils.getBean(NioSelector.class);
        String data = this.getHeader("x-data");
        Map<String, Object> map = GsonBuilderUtil.create().fromJson(data, new TypeToken<Map<String, Object>>() {
        }.getType());
        log.info("接收到新到Websocket连接");
        String name = (String) map.get("name");
        String clientId = (String) map.get("clientId");
        String nonce = (String) map.get("nonce");
        long timestamp = NumberUtil.parseLong(map.getOrDefault("timestamp", "0").toString());
        String sign = (String) map.remove("sign");

        Connect connect = null;
        try {
            long expire = timestamp + 60000;
            if (expire < System.currentTimeMillis()) {
                log.error("签名错误:签名时间验证失败,请确认服务器时间是否同步");
                throw new CodeException(ErrorCode.SERVER_ERROR);
            }
            if (!Objects.equals(clientService.getClientId(), clientId)) {
                log.error("签名错误:当前客户端已加入其他系统，如需重新加入，请删除当前路径下config.json，重启后重新加入");
                throw new CodeException(ErrorCode.SERVER_ERROR);
            }
            String dataSign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
            if (!Objects.equals(dataSign, sign)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "签名错误:签名验证失败.");
            }
            log.info("开始查询虚拟机信息:{}", name);
            connect = ConnectFactory.create();
            int port = 0;
            try {
                String xml = connect.domainLookupByName(name).getXMLDesc(0);
                port = VncUtil.getVnc(xml);
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

    @SuppressWarnings("unchecked")
    public String getHeader(String name) {
        Map<String, List<String>> headers = (Map<String, List<String>>) session.getUserProperties().get("Header");
        List<String> values = headers.get(name);
        if (ObjectUtils.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    @OnClose
    public void onVncClose() {
        this.close();
    }

    @SneakyThrows
    @OnMessage
    public void onVncMessage(byte[] messages, Session session) {
        this.vncClient.send(messages);
    }

    @OnError
    public void onVncError(Session session, Throwable error) {
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
        this.close();
    }

    @Override
    public void onError(Exception err) {
        this.close();
    }

    @Override
    public void onData(ByteBuffer buffer) {
        try {
            this.session.getBasicRemote().sendBinary(buffer);
        } catch (Exception err) {
            this.close();
        }
    }
}
