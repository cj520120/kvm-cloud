package cn.ruiyun.cloud.management.websocket.cluster.process;

import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.springframework.plugin.core.Plugin;


/**
 * @author chenjun
 */
public interface ClusterMessageProcess extends Plugin<Integer> {
    /**
     * 处理通知消息
     *
     * @param msg
     */
    void process(NotifyData<?> msg);

}
