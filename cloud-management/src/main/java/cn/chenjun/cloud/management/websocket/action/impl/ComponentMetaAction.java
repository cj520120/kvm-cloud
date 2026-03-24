package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.servcie.MetaService;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ComponentMetaAction implements WsAction {
    @Autowired
    private MetaService metaService;

    @Override
    public void doAction(WebSocket webSocket, WsRequest msg) throws IOException {

        if (webSocket.getContext() != null) {
            ComponentContext context = (ComponentContext) webSocket.getContext();
            String requestId = msg.getData().get("request-id").toString();
            String type = msg.getData().get("type").toString();
            String ip = msg.getData().get("ip").toString();
            Map<String, Object> map = new HashMap<>();
            map.put("request-id", requestId);
            try {
                List<MetaData> dataList = null;
                if ("meta-data".equals(type)) {
                    MetaData metaData = metaService.loadAllGuestMetaData(context.getNetworkId(), ip);
                    dataList = Collections.singletonList(metaData);
                } else if ("user-data".equals(type)) {
                    dataList = this.metaService.findGuestInitData(context.getNetworkId(), ip);
                } else if ("vendor-data".equals(type)) {
                    dataList = this.metaService.findGuestVendorData(context.getNetworkId(), ip);
                } else {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的类型");
                }
                String response = buildMetaResponse(dataList);
                map.put("data", response);
                map.put("status", HttpStatus.OK.value());
            } catch (Exception e) {
                log.error("获取组件元数据失败", e);
                map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            webSocket.send(WsMessage.builder().command(Constant.SocketCommand.COMPONENT_META_RESPONSE).data(map).build());

        }
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_META_REQUEST;
    }


    private String buildMetaResponse(List<MetaData> partList) {
        partList = partList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        if (!partList.isEmpty()) {
            if (partList.size() == 1) {
                MetaData metaData = partList.get(0);
                sb.append(metaData.getType().getFirstLine());
                sb.append(metaData.getBody());
            } else {
                String boundary = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
                sb.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\n");
                sb.append("MIME-Version: 1.0\n");
                sb.append("Number-Attachments: ").append(partList.size()).append("\n\n");
                for (int i = 0; i < partList.size(); i++) {
                    MetaData metaData = partList.get(i);
                    sb.append(buildConfig(metaData, boundary, i));
                }
                sb.append("--").append(boundary).append("--");
            }
        }
        return sb.toString();
    }

    private String buildConfig(MetaData metaData, String boundary, int index) {
        String firstLine = metaData.getType().getFirstLine();
        String contextType = metaData.getType().getContextType();
        String config = metaData.getBody().replace(firstLine, "").trim();
        String sb = "--" + boundary + "\n" +
                "Content-Type: " + contextType + "; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: attachment; filename=\"part-00" + index + "\"\n\n" +
                config +
                "\n";
        return sb;
    }
}
