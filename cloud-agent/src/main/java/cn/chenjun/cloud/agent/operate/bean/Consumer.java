package cn.chenjun.cloud.agent.operate.bean;

import org.libvirt.Connect;

/**
 * @author chenjun
 */
@FunctionalInterface
public interface Consumer<T, V> {
    /**
     * 处理数据
     *
     * @param connect
     * @param param
     * @return
     * @throws Exception
     */
    T dispatch(Connect connect, V param) throws Exception;
}
