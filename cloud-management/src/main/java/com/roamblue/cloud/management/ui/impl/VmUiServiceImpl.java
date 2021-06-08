package com.roamblue.cloud.management.ui.impl;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.annotation.Rule;
import com.roamblue.cloud.management.bean.*;
import com.roamblue.cloud.management.service.InstanceService;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.ui.VmUiService;
import com.roamblue.cloud.management.util.VMType;
import com.roamblue.cloud.management.util.LockKeyUtil;
import com.roamblue.cloud.management.util.RuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VmUiServiceImpl extends AbstractUiService implements VmUiService {
    @Autowired
    protected LockService lockService;
    @Autowired
    private InstanceService vmService;

    @Override
    public ResultUtil<List<VmInfo>> listAllVm() {
        return this.call(() -> vmService.listAllVm());
    }

    @Override
    public ResultUtil<List<VmInfo>> search(int clusterId, int hostId, int groupId, String type, String status) {

        return this.call(() -> vmService.search(clusterId, hostId, groupId, type, status));
    }

    @Override
    public ResultUtil<VmInfo> findVmById(int vmId) {
        return this.call(() -> vmService.findVmById(vmId));
    }


    @Override
    public ResultUtil<List<VmStatisticsInfo>> listVmStatistics(int vmId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(vmId), () -> this.call(() -> vmService.listVmStatisticsById(vmId)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> modifyInstance(int vmId, String description, int calculationSchemeId, int groupId) {

        if (StringUtils.isEmpty(description)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "VM备注不能为空");
        }
        return lockService.run(LockKeyUtil.getInstanceLockKey(vmId), () -> this.call(() -> vmService.getVmServiceByVmId(vmId).modify(vmId, description, calculationSchemeId, groupId)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VncInfo> findVncByVmId(int id) {
        return this.call(() -> vmService.findVncById(id));
    }

    @Override
    public ResultUtil<VmInfo> create(String name, int clusterId, int storageId, int hostId, int calculationSchemeId, int templateId, long size, int networkId, int groupId) {

        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }

        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        if (calculationSchemeId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "计算方案不能为空");
        }
        if (templateId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "系统模版不能为空");
        }
        if (networkId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "网络不能为空");
        }
        return this.call(() -> vmService.getVmServiceByType(VMType.GUEST).create(name, calculationSchemeId, clusterId, storageId, hostId, templateId, size, networkId, groupId));
    }

    @Override
    public ResultUtil<VmInfo> start(int id, int hostId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).start(id, hostId)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> stop(int id, boolean force) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).stop(id, force)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> reboot(int id, boolean force) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).reboot(id, force)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> reInstall(int id, int templateId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).reInstall(id, templateId)), 1, TimeUnit.MINUTES);
    }

    @Override
    @Rule(min = RuleType.ADMIN)
    public ResultUtil<TemplateInfo> createTemplate(int id, String name) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).createTemplate(id, name)), 1, TimeUnit.HOURS);
    }

    @Override
    public ResultUtil<VmInfo> destroyVmById(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> {
            vmService.getVmServiceByVmId(id).destroy(id);
            return vmService.findVmById(id);
        }), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> resume(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).resume(id)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> attachCdRoom(int id, int iso) {

        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).changeCdRoom(id, iso)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VmInfo> detachCdRoom(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).changeCdRoom(id, 0)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VolumeInfo> attachDisk(int id, int volume) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).attachDisk(id, volume)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VolumeInfo> detachDisk(int id, int volume) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).detachDisk(id, volume)), 1, TimeUnit.MINUTES);
    }

}
