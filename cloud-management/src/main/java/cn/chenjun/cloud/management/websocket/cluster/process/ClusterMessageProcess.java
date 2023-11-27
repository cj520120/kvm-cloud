package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.management.websocket.message.NotifyData;

/**
 * @author chenjun
 */
public interface ClusterMessageProcess {
    /**
     * 处理通知消息
     *
     * @param msg
     */
    void process(NotifyData<?> msg);

    /**
     * 处理消息类型
     *
     * @return
     */
    int getType();
}
