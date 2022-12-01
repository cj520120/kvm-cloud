package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.bean.StorageCreateRequest;
import cn.roamblue.cloud.common.bean.StorageDestroyRequest;
import cn.roamblue.cloud.common.bean.StorageInfoRequest;
import org.libvirt.Connect;

import java.util.List;

/**
 * @author chenjun
 */
public interface StorageOperate {
    /**
     * 获取存储池信息
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    StorageModel getStorageInfo(Connect connect, StorageInfoRequest request) throws Exception;

    /**
     * 批量获取存储池信息
     * @param connect
     * @param batchRequest
     * @return
     * @throws Exception
     */
    List<StorageModel> batchStorageInfo(Connect connect, List<StorageInfoRequest> batchRequest) throws Exception;

    /**
     * 初始化存储池信息
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    StorageModel create(Connect connect, StorageCreateRequest request) throws Exception;

    /**
     * 销毁存储池
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroy(Connect connect, StorageDestroyRequest request) throws Exception;
}
