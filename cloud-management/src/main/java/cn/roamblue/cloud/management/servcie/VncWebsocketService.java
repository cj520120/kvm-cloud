package cn.roamblue.cloud.management.servcie;

import cn.roamblue.cloud.management.data.entity.ComponentEntity;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.GuestVncEntity;
import cn.roamblue.cloud.management.data.mapper.ComponentMapper;
import cn.roamblue.cloud.management.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.data.mapper.GuestVncMapper;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Objects;

@ServerEndpoint(value = "/api/vnc/{id}")
@Component
public class VncWebsocketService {

    private VncWebSocketProxy proxy;
    private Session session;

    @SneakyThrows
    @OnOpen
    public void onVncConnect(Session session, @PathParam(value = "id") int id) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(id);
        if (guest == null || !Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING)) {
            session.close();
            return;
        }
        ComponentMapper componentMapper = SpringContextUtils.getBean(ComponentMapper.class);
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("network_id", guest.getNetworkId()).eq("component_type", Constant.ComponentType.VNC).last("limit 0,1"));
        if (component == null) {
            session.close();
            return;
        }
        guest = guestMapper.selectById(component.getGuestId());
        if (guest == null || !Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING)) {
            session.close();
            return;
        }
        GuestVncMapper guestVncMapper = SpringContextUtils.getBean(GuestVncMapper.class);
        GuestVncEntity guestVnc = guestVncMapper.selectById(id);
        if (guestVnc == null) {
            return;
        }
        String uri = "ws://" + guest.getGuestIp() + ":8080/websockify/?token=" + guestVnc.getToken();
        this.proxy = new VncWebSocketProxy(session, new URI(uri));
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
            } catch (Exception err) {

            } finally {
                this.session = null;
            }
        }
        if (this.proxy != null) {
            try {
                this.proxy.close();
            } catch (Exception err) {

            } finally {
                this.proxy = null;
            }
        }
    }

    public class VncWebSocketProxy extends WebSocketClient {

        private final Session session;

        public VncWebSocketProxy(Session session, URI serverUri) {
            super(serverUri, new Draft_6455());
            this.session = session;

        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {

        }

        @Override
        public void onMessage(String s) {
        }

        @SneakyThrows
        @Override
        public void onMessage(ByteBuffer bytes) {
            session.getBasicRemote().sendBinary(bytes);
        }

        @SneakyThrows
        @Override
        public void onClose(int i, String s, boolean b) {
            this.session.close();
            this.close();
        }

        @SneakyThrows
        @Override
        public void onError(Exception e) {
            this.session.close();
            this.close();
        }
    }
}
