package cn.roamblue.cloud.agent.operate.impl;

import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.agent.StorageRequest;
import org.libvirt.Connect;

public class StorageOperateImpl implements StorageOperate {
    @Override
    public StorageModel create(Connect connect, StorageRequest request) throws Exception {
        return null;
    }

    @Override
    public void destroy(Connect connect, String name) {

    }
}
