package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/component/ws")
@EqualsAndHashCode
public class ComponentNotify {
    private static final CopyOnWriteArraySet<ComponentNotify> NETWORK_CLIENT_SESSIONS = new CopyOnWriteArraySet<>();
    private Session session;

    private int networkId;

    public synchronized static <T> void sendNotify(int networkId, NotifyMessage<T> message) {

        WsMessage<NotifyMessage<T>> wsMessage = WsMessage.<NotifyMessage<T>>builder().command(Constant.SocketCommand.NOTIFY).data(message).build();
        String msg = GsonBuilderUtil.create().toJson(wsMessage);
        for (ComponentNotify client : NETWORK_CLIENT_SESSIONS) {
            if (client.networkId == networkId) {
                try {
                    client.session.getBasicRemote().sendText(msg);
                } catch (Exception e) {
                    log.info("写入通知消息出错。", e);
                }
            }
        }
    }

    @SneakyThrows
    @OnOpen
    public void onConnect(Session session) {
        this.session = session;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        NETWORK_CLIENT_SESSIONS.remove(this);
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String jsonMsg) {
        NotifyMessage<?> msg = GsonBuilderUtil.create().fromJson(jsonMsg, NotifyMessage.class);
        if (msg == null) {
            log.error("未知的请求:{}", jsonMsg);
            this.session.close();
            return;
        }
        if (msg.getType() == Constant.SocketCommand.CLIENT_CONNECT) {
            NETWORK_CLIENT_SESSIONS.remove(this);
            Map<String, Object> params = (Map<String, Object>) msg.getData();
            int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
            String nonce = params.getOrDefault("nonce", "").toString();
            String sign = params.getOrDefault("sign", "").toString();
            NetworkEntity network = SpringContextUtils.getBean(NetworkMapper.class).selectById(networkId);
            if (DigestUtil.md5Hex(network.getSecret() + ":" + networkId + ":" + nonce).equals(sign)) {
                NETWORK_CLIENT_SESSIONS.add(this);
                this.networkId = networkId;
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_SUCCESS).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            } else {
                WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_TOKEN_ERROR).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }
            return;
        }
        if (this.networkId == 0) {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.NOT_LOGIN).build();
            session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
        }
        switch (msg.getType()) {
            case Constant.SocketCommand.DNS_REQUEST: {
                NotifyMessage<List<DnsModel>> sendMsg = NotifyMessage.<List<DnsModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(SpringContextUtils.getBean(DnsService.class).listLocalNetworkDns(this.networkId)).build();
                WsMessage<NotifyMessage<List<DnsModel>>> wsMessage = WsMessage.<NotifyMessage<List<DnsModel>>>builder().command(Constant.SocketCommand.NOTIFY).data(sendMsg).build();
                String sendJson = GsonBuilderUtil.create().toJson(wsMessage);
                this.session.getBasicRemote().sendText(sendJson);
            }
            break;
            case Constant.SocketCommand.VNC_REQUEST: {
                NotifyMessage<List<VncModel>> sendMsg = NotifyMessage.<List<VncModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(SpringContextUtils.getBean(VncService.class).listVncByNetworkId(this.networkId)).build();
                WsMessage<NotifyMessage<List<VncModel>>> wsMessage = WsMessage.<NotifyMessage<List<VncModel>>>builder().command(Constant.SocketCommand.NOTIFY).data(sendMsg).build();
                String sendJson = GsonBuilderUtil.create().toJson(wsMessage);
                this.session.getBasicRemote().sendText(sendJson);
            }
            break;
            default:
                break;
        }
    }


    @OnClose
    public void onClose() {
        NETWORK_CLIENT_SESSIONS.remove(this);
    }


}
