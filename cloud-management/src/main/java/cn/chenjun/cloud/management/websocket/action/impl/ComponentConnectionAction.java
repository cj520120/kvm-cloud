package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.manager.ComponentClientManager;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class ComponentConnectionAction implements WsAction {

    private static final int COMPONENT_STATUS_SUCCESS = 0;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private LockRunner lockRunner;

    @Override
    public void doAction(WebSocket webSocket, WsRequest msg) throws IOException {
        Map<String, Object> params = msg.getData();
        int networkId = NumberUtil.parseInt(params.getOrDefault("networkId", "0").toString());
        NetworkEntity network = this.networkService.getNetworkInfo(networkId);
        String sign = params.remove("sign").toString();
        if (this.signature(params, network.getSecret()).equals(sign)) {
            String sessionId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            ComponentContext context = GsonBuilderUtil.create().fromJson(params.toString(), ComponentContext.class);
            context.setSessionId(sessionId);
            webSocket.login(context);
            ComponentClientManager.addComponentClient(context.getComponentId(), webSocket);
            ComponentGuestEntity loginComponentGuest = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
                ComponentGuestEntity componentGuest = componentService.findComponentGuestById(context.getComponentGuestId());
                if (componentGuest != null) {
                    if (context.getStatus() == COMPONENT_STATUS_SUCCESS) {
                        componentGuest.setErrorCount(0);
                        componentGuest.setStatus(Constant.ComponentGuestStatus.ONLINE);
                    } else {
                        componentGuest.setStatus(Constant.ComponentGuestStatus.OFFLINE);
                        componentGuest.setErrorCount(1000);
                    }
                    componentGuest.setSessionId(sessionId);
                    componentGuest.setLastActiveTime(new Date(System.currentTimeMillis()));
                    componentService.updateComponentGuest(componentGuest);
                    log.info("component guest online {}", componentGuest);
                }
                return componentGuest;
            });
            WsMessage<Void> wsMessage;
            if (loginComponentGuest == null) {
                wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
            } else {
                wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_SUCCESS).build();
            }
            webSocket.send(wsMessage);
        } else {
            WsMessage<Void> wsMessage = WsMessage.<Void>builder().command(Constant.SocketCommand.COMPONENT_CONNECT_FAIL).build();
            webSocket.send(wsMessage);
        }
    }

    public String signature(Map<String, Object> headers, String secret) {
        List<String> signatureDataList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            signatureDataList.add(key + ":" + value);
        }
        Collections.sort(signatureDataList);
        String signatureStr = String.join("&", signatureDataList) + secret;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = md.digest(signatureStr.getBytes());
            // 将字节数组转 16 进制字符串（小写）
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : md5Bytes) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }


    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_CONNECT;
    }
}
