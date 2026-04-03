package cn.chenjun.cloud.management.websocket.action.impl.agent;

import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.common.socket.packet.data.base.MapData;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.websocket.action.WsAction;
import cn.chenjun.cloud.management.websocket.listen.client.Client;
import cn.chenjun.cloud.management.websocket.listen.context.HostContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class AgentTaskCallbackAction implements WsAction<byte[]> {
    @Autowired
    private TaskService taskService;


    @Override
    public void doAction(Client webSocket, WsMessage<byte[]> msg) throws IOException {
        HostContext context = (HostContext) webSocket.getContext();
        if (Objects.isNull(context)) {
            webSocket.close();
            return;
        }
        MapData map = MapData.fromBytes(msg.getData());
        String taskId = map.getOrDefault("taskId", "").toString();
        String data = map.getOrDefault("data", "").toString();
        taskService.submitTaskFinish(taskId, data);
    }

    @Override
    public int getCommand() {
        return Constant.SocketCommand.AGENT_TASK_CALLBACK;
    }
}
