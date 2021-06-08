package com.roamblue.cloud.agent.service;

import com.roamblue.cloud.common.agent.VmInfoModel;
import com.roamblue.cloud.common.agent.VmModel;
import com.roamblue.cloud.common.agent.VmStaticsModel;

import java.util.List;

public interface KvmVmService {
    List<VmInfoModel> listVm();

    List<VmStaticsModel> listVmStatics();

    VmInfoModel findByName(String name);


    void restart(String name);

    void destroy(String name);

    void stop(String name);

    void attachDevice(String name, String xml);

    void detachDevice(String name, String xml);

    VmInfoModel start(VmModel info);

    void updateDevice(String name, String xml);
}
