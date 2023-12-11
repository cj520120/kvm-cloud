package cn.chenjun.cloud.management.websocket.cluster.process;

/**
 * @author chenjun
 */
public abstract class AbstractClusterMessageProcess implements ClusterMessageProcess {
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
}
