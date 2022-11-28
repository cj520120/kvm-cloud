package cn.roamblue.cloud.agent.operate.impl;

import cn.roamblue.cloud.agent.operate.StorageOperate;
import org.libvirt.Connect;

import java.util.Map;

public class StorageOperateImpl implements StorageOperate {
    @Override
    public void create(Connect connect, String name, Map<String, Object> param) throws Exception {

    }

    @Override
    public void destroy(Connect connect, String name) {

    }
}
