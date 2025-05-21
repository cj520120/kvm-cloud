package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.event.EventObject;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.client.WsClient;
import cn.chenjun.cloud.management.websocket.client.owner.VncOwner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenjun
 */
@Slf4j
@ServerEndpoint(value = "/api/vnc/{id}")
@Component
public class VncWsService extends AbstractWsService<ByteBuffer> {


    @Override
    protected MessageHandler.Whole<ByteBuffer> createMessageHandler(Session session) {
        MessageHandler.Whole<ByteBuffer> handler = new MessageHandler.Whole<ByteBuffer>() {
            @SneakyThrows
            @Override
            public void onMessage(ByteBuffer message) {
                WsClient<VncOwner> client = (WsClient<VncOwner>) session.getUserProperties().get("client");
                if (Objects.isNull(client)) return;
                client.getOwner().send(message);
            }
        };
        return handler;
    }

    @SneakyThrows
    @OnOpen
    @Override
    public void onConnect(Session session) {
        super.onConnect(session);
        int id = Integer.parseInt(session.getPathParameters().get("id"));
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(id);
        if (guest == null || guest.getHostId() <= 0) {
            session.close();
            log.info("虚拟机未运行或不存在.id={}", id);
            return;
        }
        if (!Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING) && !Objects.equals(guest.getStatus(), Constant.GuestStatus.STARTING)) {
            session.close();
            log.info("虚拟机当前状态不属于运行状态.id={}", id);
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

        log.info("开始连接虚拟机VNC.id={},vnc={}", id, url.toASCIIString());
        VncOwner owner = new VncOwner(new URI(url.toASCIIString()), header);
        owner.connect();
        owner.onClose.addEvent(new EventHandler<Void>() {
            @SneakyThrows
            @Override
            public void fire(Object sender, EventObject<Void> obj) {
                session.close();
            }
        });
        owner.onMessage.addEvent(new EventHandler<ByteBuffer>() {
            @SneakyThrows
            @Override
            public void fire(Object sender, EventObject<ByteBuffer> obj) {
                session.getBasicRemote().sendBinary(obj.getEvent());
            }
        });
        WsClient<VncOwner> client = WsClient.<VncOwner>builder().owner(owner).session(session).build();
        session.getUserProperties().put("client", client);
    }

    @SneakyThrows
    @OnClose
    @Override
    public void onClose(Session session) {
        WsClient<VncOwner> client = (WsClient<VncOwner>) session.getUserProperties().get("client");
        if (client != null) {
            client.getOwner().close();
        }
        super.onClose(session);
    }

}
