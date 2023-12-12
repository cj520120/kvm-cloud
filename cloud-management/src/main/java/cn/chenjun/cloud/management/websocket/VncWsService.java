package cn.chenjun.cloud.management.websocket;

import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestVncEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.GuestVncMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.client.VncClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.Objects;

/**
 * @author chenjun
 */
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
        if (guest == null || !Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING)) {
            session.close();
            return;
        }
        ComponentMapper componentMapper = SpringContextUtils.getBean(ComponentMapper.class);
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, guest.getNetworkId()).eq(ComponentEntity.COMPONENT_TYPE, Constant.ComponentType.ROUTE).last("limit 0,1"));
        if (component == null) {
            session.close();
            return;
        }
        GuestVncMapper guestVncMapper = SpringContextUtils.getBean(GuestVncMapper.class);
        GuestVncEntity guestVnc = guestVncMapper.selectById(id);
        if (guestVnc == null) {
            return;
        }
        String uri = "ws://" + component.getBasicComponentVip() + ":8080/websockify/?token=" + guestVnc.getToken();
        this.proxy = new VncClient(session, new URI(uri));
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
