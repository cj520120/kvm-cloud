package cn.chenjun.cloud.management.websocket.listen;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.SpringContextUtils;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.VncProxyContext;
import cn.hutool.core.util.NumberUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.MessageHandler;
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
    protected MessageHandler.Whole<ByteBuffer> createMessageHandler(WebSocket webSocket) {
        MessageHandler.Whole<ByteBuffer> handler = new MessageHandler.Whole<ByteBuffer>() {
            @Override
            public void onMessage(ByteBuffer message) {
                VncProxyContext client = (VncProxyContext) webSocket.getContext();
                if (Objects.isNull(client)) return;
                client.send(message);
            }
        };
        return handler;
    }
    @SneakyThrows
    @Override
    protected void onConnection(WebSocket webSocket) {
        super.onConnection(webSocket);
        int id = NumberUtil.parseInt(webSocket.getSession().getPathParameters().get("id"));
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(id);
        if (guest == null || guest.getHostId() <= 0) {
            webSocket.close();
            log.info("虚拟机未运行或不存在.id={}", id);
            return;
        }
        if (!Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING) && !Objects.equals(guest.getStatus(), Constant.GuestStatus.STARTING)) {
            webSocket.close();
            log.info("虚拟机当前状态不属于运行状态.id={}", id);
            return;
        }
        HostEntity host = SpringContextUtils.getBean(HostMapper.class).selectById(guest.getHostId());
        URI url = new URI(host.getUri().replaceFirst("http", "ws") + "/api/vnc");
        String nonce = String.valueOf(System.nanoTime());
        Map<String, Object> map = new HashMap<>(6);
        map.put("name", guest.getName());
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        String sign = AppUtils.sign(map, host.getClientId(), host.getClientSecret(), nonce);
        map.put("sign", sign);
        String data = GsonBuilderUtil.create().toJson(map);
        Map<String, String> header = new HashMap<>(1);
        header.put("x-data", data);
        log.info("开始连接虚拟机VNC.id={},vnc={}", id, url.toASCIIString());
        VncProxyContext context = new VncProxyContext(new URI(url.toASCIIString()), header);
        context.connect();
        context.onClose.addEvent((sender, obj) -> webSocket.close());
        context.onMessage.addEvent((sender, obj) -> webSocket.send(obj.getEvent()));
        webSocket.onClose.addEvent((sender, obj) -> context.close());
        webSocket.setContext(context);
    }


}
