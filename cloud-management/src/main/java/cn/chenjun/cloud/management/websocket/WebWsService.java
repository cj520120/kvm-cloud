package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author chenjun
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/ws/")
@EqualsAndHashCode
public class WebWsService {
    private static final CopyOnWriteArraySet<WebWsService> CLIENT_SESSION_LIST = new CopyOnWriteArraySet<>();
    private Session session;
    private LoginUserModel loginInfo;

    public synchronized static <T> void sendNotify(NotifyMessage<T> message) {

        WsMessage<NotifyMessage<T>> wsMessage = WsMessage.<NotifyMessage<T>>builder().command(Constant.SocketCommand.NOTIFY).data(message).build();
        String msg = GsonBuilderUtil.create().toJson(wsMessage);
        for (WebWsService client : CLIENT_SESSION_LIST) {
            try {
                client.session.getBasicRemote().sendText(msg);
            } catch (Exception e) {
                log.info("写入通知消息出错。",e);
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
        CLIENT_SESSION_LIST.remove(this);
    }

    @SneakyThrows
    @OnMessage
    public void onMessage(String jsonMsg) {
        NotifyMessage<?> msg = GsonBuilderUtil.create().fromJson(jsonMsg, NotifyMessage.class);
        if (msg!=null&&msg.getType() == Constant.SocketCommand.CLIENT_CONNECT) {
            String token = (String) msg.getData();
            try {
                ResultUtil<LoginUserModel> resultUtil = SpringContextUtils.getBean(UserService.class).getUserIdByToken(token);
                if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                    this.loginInfo = resultUtil.getData();
                    CLIENT_SESSION_LIST.add(this);
                    WsMessage<Void> wsMessage= WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_SUCCESS).build();
                    session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
                } else {
                    throw new CodeException(ErrorCode.NO_LOGIN_ERROR);
                }
            } catch (Exception err) {
                WsMessage<Void> wsMessage= WsMessage.<Void>builder().command(Constant.SocketCommand.LOGIN_TOKEN_ERROR).build();
                session.getBasicRemote().sendText(GsonBuilderUtil.create().toJson(wsMessage));
            }
        }else{
            log.error("未知的请求:{}",jsonMsg);
            this.session.close();
        }
    }

    @OnClose
    public void onClose() {
        CLIENT_SESSION_LIST.remove(this);
    }


}
