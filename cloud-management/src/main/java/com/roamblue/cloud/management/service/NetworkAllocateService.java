package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.VmNetworkInfo;

public interface NetworkAllocateService {
    VmNetworkInfo allocateManagerAddress(int networkId, int vmId);

    VmNetworkInfo allocateGuestAddress(int networkId, int vmId);

    String getType();
}
