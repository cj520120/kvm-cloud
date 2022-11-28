package cn.roamblue.cloud.agent.operate;

import org.libvirt.Connect;

import java.util.Map;

/**
 * @author chenjun
 */
public interface StorageOperate {
    /**
     * 初始化存储池信息
     *
     * @param connect
     * @param name
     * @param param
     * @throws Exception
     */
    void create(Connect connect, String name, Map<String, Object> param) throws Exception;

    /**
     *
     * @param connect
     * @param name
     */
    void destroy(Connect connect,String name);
}
