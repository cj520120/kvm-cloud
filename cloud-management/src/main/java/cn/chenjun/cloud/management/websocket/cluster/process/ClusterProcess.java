package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.management.websocket.message.NotifyData;

public interface ClusterProcess {
    void process(NotifyData<?> msg);

    int getType();
}
