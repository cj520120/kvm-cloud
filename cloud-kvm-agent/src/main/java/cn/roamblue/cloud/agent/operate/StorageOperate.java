package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.agent.StorageRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface StorageOperate {
    /**
     * 初始化存储池信息
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    StorageModel create(Connect connect, StorageRequest request) throws Exception;

    /**
     * 销毁存储池
     *
     * @param connect
     * @param name
     * @throws Exception
     */
    void destroy(Connect connect, String name) throws Exception;
}
