package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.*;

import java.util.List;

public interface VmUiService {
    ResultUtil<List<VmInfo>> listAllVm();

    ResultUtil<List<VmInfo>> search(int clusterId, int hostId, int groupId, String type, String status);

    ResultUtil<VmInfo> findVmById(int vmId);

    ResultUtil<List<VmStatisticsInfo>> listVmStatistics(int vmId);

    ResultUtil<VmInfo> modifyInstance(int vmId, String description, int calculationSchemeId, int groupId);

    ResultUtil<VncInfo> findVncByVmId(int id);

    ResultUtil<VmInfo> create(String name, int clusterId, int storageId, int hostId, int calculationSchemeId, int templateId, long size, int networkId, int groupId);

    ResultUtil<VmInfo> start(int id, int hostId);

    ResultUtil<VmInfo> stop(int id, boolean force);

    ResultUtil<VmInfo> reboot(int id, boolean force);

    ResultUtil<VmInfo> reInstall(int vmId, int templateId);

    ResultUtil<TemplateInfo> createTemplate(int id, String name);

    ResultUtil<VmInfo> destroyVmById(int id);

    ResultUtil<VmInfo> resume(int id);

    ResultUtil<VmInfo> attachCdRoom(int id, int iso);

    ResultUtil<VmInfo> detachCdRoom(int id);

    ResultUtil<VolumeInfo> attachDisk(int id, int volume);

    ResultUtil<VolumeInfo> detachDisk(int id, int volume);
}
