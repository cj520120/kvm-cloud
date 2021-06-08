package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.VmInfo;
import com.roamblue.cloud.management.bean.VmStatisticsInfo;
import com.roamblue.cloud.management.bean.VncInfo;

import java.util.List;

public interface InstanceService {


    VmService getVmServiceByVmId(int id);

    VmService getVmServiceByType(String type);

    VmInfo findVmById(int id);

    VncInfo findVncById(int vmId);

    List<VmInfo> search(int clusterId, int hostId, int groupId, String type, String status);

    List<VmInfo> listAllVm();

    List<VmStatisticsInfo> listVmStatisticsById(int vmId);
}
