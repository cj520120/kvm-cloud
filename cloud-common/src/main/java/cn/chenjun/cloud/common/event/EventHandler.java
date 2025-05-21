package cn.chenjun.cloud.common.event;

public interface EventHandler<T> {
    /**
     * 触发事件
     *
     * @param sender 触发着
     * @param obj    触发消息
     * @throws Exception
     */
    void fire(Object sender, EventObject<T> obj);

}
