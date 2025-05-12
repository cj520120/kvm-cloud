package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.management.websocket.message.NotifyData;

/**
 * @author chenjun
 */
public abstract class AbstractClusterMessageProcess<T> implements ClusterMessageProcess {
    @Override
    public boolean supports(Integer type) {
        return type.equals(this.getType());
    }

    /**
     * 处理消息类型
     *
     * @return
     */
    protected abstract int getType();

    @Override
    public void process(NotifyData<?> msg) {
        if (msg.getType() == this.getType()) {
            this.doProcess((NotifyData<T>) msg);
        }
    }

    protected abstract void doProcess(NotifyData<T> msg);


}
