package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.agent.sock.NioCallback;
import cn.chenjun.cloud.agent.sock.NioClient;
import cn.chenjun.cloud.agent.sock.NioSelector;
import cn.chenjun.cloud.agent.ws.client.WsClient;
import cn.chenjun.cloud.common.socket.packet.data.host.VncData;
import cn.chenjun.cloud.common.socket.packet.data.host.VncDisconnect;
import cn.chenjun.cloud.common.util.Constant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class VncManager {
    private static final Map<String, VncBind> VNC_BINDS = new ConcurrentHashMap<>();

    public static boolean connect(WsClient client, String id, String name) {
        Connect connect = null;
        try {
            connect = ConnectFactory.create();
            String xml = connect.domainLookupByName(name).getXMLDesc(0);
            int port = GraphicsUtil.getGraphics(xml).getPort();
            VncBind vncBind = new VncBind(id, client, "127.0.0.1", port);
            VNC_BINDS.put(id, vncBind);
            return true;
        } catch (Exception err) {
            log.error("查询虚拟机信息信息失败.name={}", name, err);
            client.sendCommand(Constant.SocketCommand.VNC_DISCONNECT);
            return false;
        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception ignored) {

            }
        }
    }

    public static void disconnect(String id) {
        VncBind vncBind = VNC_BINDS.remove(id);
        if (vncBind != null) {
            vncBind.close();
            sendDisconnect(vncBind.client, id);
        }
    }

    private static void sendDisconnect(WsClient client, String id) {
        try {
            VncDisconnect data = VncDisconnect.builder().id(id).build();
            client.sendBuffer(Constant.SocketCommand.VNC_DISCONNECT, data.toBytes());
        } catch (Exception ignored) {

        }
    }

    public static void forward(WsClient wsClient, byte[] data) {
        VncData vncData = VncData.fromBytes(data);

        VncBind vncBind = VNC_BINDS.get(vncData.getId());
        if (vncBind != null) {
            try {
                vncBind.sendVncData(vncData.getData());
            } catch (Exception ignored) {

            }
        } else {
            sendDisconnect(wsClient, vncData.getId());
        }
    }

    @Slf4j
    static class VncBind implements NioCallback {
        private final String id;
        private final WsClient client;
        private final NioClient vncClient;

        @SneakyThrows
        public VncBind(String id, WsClient client, String host, int port) {
            this.id = id;
            this.client = client;
            NioSelector nioSelector = SpringContextUtils.getBean(NioSelector.class);
            this.vncClient = nioSelector.createClient(host, port, this);
            log.info("建立Vnc连接:id={},tcp://{}:{}", id, host, port);
        }

        @Override
        public void onClose() {
            log.info("Vnc连接断开:id={}", id);
            this.close();
        }

        @Override
        public void onError(Exception err) {
            log.error("Vnc连接出错:id={}", id, err);
            this.close();
        }

        @Override
        public void onData(ByteBuffer buffer) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            VncData vncData = VncData.builder().id(this.id).data(data).build();
            byte[] bytes = vncData.toBytes();
            log.debug("Vnc数据接收:id={},开始转发到管理端,数据长度:{}", id, bytes.length);
            try {
                client.sendBuffer(Constant.SocketCommand.VNC_DATA, bytes); // 转发数据到前端
            } catch (Exception err) {
                log.error("Vnc数据转发出错:id={}", id, err);
                this.close();
            }
        }

        public void sendVncData(byte[] data) {
            try {
                log.debug("Vnc数据发送:id={},开始转发到虚拟机,数据长度:{}", id, data.length);
                this.vncClient.send(data);
            } catch (Exception err) {
                log.error("Vnc数据发送出错:id={}", id, err);
                this.close();
            }
        }

        public void close() {
            try {
                this.vncClient.close();
            } catch (Exception ignored) {

            }
            VncManager.disconnect(this.id);
            log.info("Vnc连接关闭:id={}", id);
        }
    }
}
