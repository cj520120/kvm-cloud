package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.management.bean.VncInfo;
import cn.roamblue.cloud.management.service.InstanceService;
import cn.roamblue.cloud.management.util.SpringContextUtils;
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

/**
 * @author chenjun
 */
@ServerEndpoint(value = "/vnc/connect/{id}")
@Component
public class VncController {
    private VncWebSocketProxy proxy;
    private Session session;

    @SneakyThrows
    @OnOpen
    public void onVncConnect(Session session, @PathParam(value = "id") int id) {
        VncInfo vnc = SpringContextUtils.getBean(InstanceService.class).findVncById(id);
        String uri = "ws://" + vnc.getIp() + ":6080/websockify/?token=" + vnc.getToken();
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