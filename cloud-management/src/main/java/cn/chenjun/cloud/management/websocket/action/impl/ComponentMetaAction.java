package cn.chenjun.cloud.management.websocket.action.impl;

import cn.chenjun.cloud.common.bean.WsMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.servcie.MetaService;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import cn.chenjun.cloud.management.util.MetaDataType;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.client.WebSocket;
import cn.chenjun.cloud.management.websocket.client.context.ComponentContext;
import cn.chenjun.cloud.management.websocket.message.WsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class ComponentMetaAction implements WsAction {
    @Autowired
    private MetaService metaService;

    @SneakyThrows
    private static String buildMetaResponse(List<MetaData> partList) {
        YAMLFactory factory = new YAMLFactory();
        factory.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
        ObjectMapper mapper = new ObjectMapper(factory);
        ObjectNode firstNode = mapper.createObjectNode();
        for (MetaData metaData : partList) {
            try {
                deepMerge(firstNode, mapper.readTree(metaData.getBody()));
            } catch (Exception e) {
                log.error("解析yaml失败,跳过该配置.原始内容:{}", metaData.getBody(), e);
            }
        }
        String sb = MetaDataType.CLOUD.getFirstLine() +
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(firstNode);
        return sb;
    }

    public static void deepMerge(ObjectNode base, JsonNode override) {
        Iterator<String> iterator = override.fieldNames();
        while (iterator.hasNext()) {
            String field = iterator.next();
            JsonNode baseNode = base.get(field);
            JsonNode overrideNode = override.get(field);
            if (baseNode instanceof ObjectNode && overrideNode instanceof ObjectNode) {
                deepMerge((ObjectNode) baseNode, overrideNode);
            } else if (baseNode instanceof ArrayNode && overrideNode instanceof ArrayNode) {
                ((ArrayNode) baseNode).addAll((ArrayNode) overrideNode);
            } else {
                base.set(field, overrideNode);
            }
        }
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.COMPONENT_META_REQUEST;
    }

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
                String response = "";
                map.put("status", HttpStatus.OK.value());
                if ("meta-data".equals(type)) {
                    MetaData metaData = metaService.loadAllGuestMetaData(context.getNetworkId(), ip);
                    List<MetaData> dataList = Collections.singletonList(metaData);
                    response = buildMetaResponse(dataList);
                } else if ("meta-data-keys".equals(type)) {
                    response = metaService.listMetaDataKeys(context.getNetworkId(), ip);
                } else if ("meta-data-info ".equals(type)) {
                    String metaKey = msg.getData().getOrDefault("meta-key", "").toString();
                    response = metaService.findMetaDataByKey(metaKey, context.getNetworkId(), ip);
                } else if ("user-data".equals(type)) {
                    List<MetaData> dataList = this.metaService.findGuestUserData(context.getNetworkId(), ip);
                    response = buildMetaResponse(dataList);
                } else if ("vendor-data".equals(type)) {
                    List<MetaData> dataList = this.metaService.findGuestVendorData(context.getNetworkId(), ip);
                    response = buildMetaResponse(dataList);
                } else {
                    map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
                map.put("data", response);
            } catch (Exception e) {
                log.error("获取组件元数据失败", e);
                map.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            webSocket.send(WsMessage.builder().command(Constant.SocketCommand.COMPONENT_META_RESPONSE).data(map).build());

        }
    }

}
