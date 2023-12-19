package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.client.VncClient;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@ServerEndpoint(value = "/api/vnc/{id}")
@Component
public class VncWsService {

    private VncClient proxy;
    private Session session;

    @SneakyThrows
    @OnOpen
    public void onVncConnect(Session session, @PathParam(value = "id") int id) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(id);
        if (guest == null || guest.getHostId() <= 0) {
            session.close();
            return;
        }
        if (!Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING) && !Objects.equals(guest.getStatus(), Constant.GuestStatus.STARTING)) {
            session.close();
            return;
        }

        HostEntity host = SpringContextUtils.getBean(HostMapper.class).selectById(guest.getHostId());
        URI url = new URI(host.getUri().replaceFirst("http", "ws") + "/api/vnc");
        String nonce = String.valueOf(System.nanoTime());
        Map<String, Object> map = new HashMap<>(6);
        map.put("name", guest.getName());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        try {
            String sign = AppUtils.sign(map, host.getClientId(), host.getClientSecret(), nonce);
            map.put("sign", sign);
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "数据签名错误");
        }
        String data = GsonBuilderUtil.create().toJson(map);
        Map<String, String> header = new HashMap<>(1);
        header.put("x-data", data);
        this.proxy = new VncClient(session, new URI(url.toASCIIString()), header);
        this.proxy.connect();

    }

    @OnClose
    public void onVncClose() {
        this.close();
    }

    @OnMessage
    public void onVncMessage(byte[] messages, Session session) {
        this.proxy.send(messages);
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
        if (this.proxy != null) {
            try {
                this.proxy.close();
            } catch (Exception ignored) {

            } finally {
                this.proxy = null;
            }
        }
    }


}
