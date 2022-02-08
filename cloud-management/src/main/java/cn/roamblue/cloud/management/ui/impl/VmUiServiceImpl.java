package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.*;
import cn.roamblue.cloud.management.service.InstanceService;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.ui.VmUiService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.VmType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
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
    @Rule(permissions = "vm.modify")
    public ResultUtil<VmInfo> modify(int vmId, String description, int calculationSchemeId, int groupId) {

        if (StringUtils.isEmpty(description)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "虚拟机描述信息不能为空");
        }
        return lockService.run(LockKeyUtil.getInstanceLockKey(vmId), () -> this.call(() -> vmService.getVmServiceByVmId(vmId).modify(vmId, description, calculationSchemeId, groupId)), 1, TimeUnit.MINUTES);
    }

    @Override
    public ResultUtil<VncInfo> findVncByVmId(int id) {
        return this.call(() -> vmService.findVncById(id));
    }

    @Rule(permissions = "vm.create")
    @Override
    public ResultUtil<VmInfo> create(String name, int clusterId, int storageId, int calculationSchemeId, int templateId, long size, int networkId, int groupId) {

        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }

        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }
        if (calculationSchemeId < 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "计算方案不能为空");
        }
        if (templateId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "模版不能为空");
        }
        if (networkId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "网络不能为空");
        }
        return this.call(() -> vmService.getVmServiceByType(VmType.GUEST).create(name, calculationSchemeId, clusterId, storageId, templateId, size, networkId, groupId));
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<VmInfo> start(int id, int hostId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).start(id, hostId)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<List<ResultUtil<VmInfo>>> batchStart(List<Integer> ids, int hostId) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtil.<List<ResultUtil<VmInfo>>>builder().data(new ArrayList<>(0)).build();
        }
        List<Supplier<ResultUtil<VmInfo>>> supplierList = ids.stream().map(id -> (Supplier<ResultUtil<VmInfo>>) () -> start(id, hostId)).collect(Collectors.toList());
        return super.batchSSupplyAsync(supplierList);
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<VmInfo> stop(int id, boolean force) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).stop(id, force)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<List<ResultUtil<VmInfo>>> batchStop(List<Integer> ids, boolean force) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtil.<List<ResultUtil<VmInfo>>>builder().data(new ArrayList<>(0)).build();
        }
        List<Supplier<ResultUtil<VmInfo>>> supplierList = ids.stream().map(id -> (Supplier<ResultUtil<VmInfo>>) () -> stop(id, force)).collect(Collectors.toList());
        return super.batchSSupplyAsync(supplierList);
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<VmInfo> reboot(int id, boolean force) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).reboot(id, force)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.status.update")
    @Override
    public ResultUtil<List<ResultUtil<VmInfo>>> batchReboot(List<Integer> ids, boolean force) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtil.<List<ResultUtil<VmInfo>>>builder().data(new ArrayList<>(0)).build();
        }
        List<Supplier<ResultUtil<VmInfo>>> supplierList = ids.stream().map(id -> (Supplier<ResultUtil<VmInfo>>) () -> reboot(id, force)).collect(Collectors.toList());
        return super.batchSSupplyAsync(supplierList);
    }

    @Rule(permissions = "vm.reinstall")
    @Override
    public ResultUtil<VmInfo> reInstall(int id, int templateId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).reInstall(id, templateId)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.template")
    @Override
    public ResultUtil<TemplateInfo> createTemplate(int id, String name) {
        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "名称不能为空");
        }
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).createTemplate(id, name)), 1, TimeUnit.HOURS);
    }

    @Rule(permissions = "vm.destroy")
    @Override
    public ResultUtil<VmInfo> destroyVmById(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> {
            vmService.getVmServiceByVmId(id).destroy(id);
            return vmService.findVmById(id);
        }), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.resume")
    @Override
    public ResultUtil<VmInfo> resume(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).resume(id)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.cd.update")
    @Override
    public ResultUtil<VmInfo> attachCdRoom(int id, int iso) {

        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).changeCdRoom(id, iso)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.cd.update")
    @Override
    public ResultUtil<VmInfo> detachCdRoom(int id) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).changeCdRoom(id, 0)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.disk.update")
    @Override
    public ResultUtil<VolumeInfo> attachDisk(int id, int volume) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).attachDisk(id, volume)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.disk.update")
    @Override
    public ResultUtil<VolumeInfo> detachDisk(int id, int volume) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(id), () -> this.call(() -> vmService.getVmServiceByVmId(id).detachDisk(id, volume)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.nic.update")
    @Override
    public ResultUtil<VmNetworkInfo> attachNetwork(int vmId, int networkId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(vmId), () -> this.call(() -> vmService.getVmServiceByVmId(vmId).attachNetwork(vmId, networkId)), 1, TimeUnit.MINUTES);
    }

    @Rule(permissions = "vm.nic.update")
    @Override
    public ResultUtil<Void> detachNetwork(int vmId, int vmNetworkId) {
        return lockService.run(LockKeyUtil.getInstanceLockKey(vmId), () -> this.call(() -> vmService.getVmServiceByVmId(vmId).detachNetwork(vmId, vmNetworkId)), 1, TimeUnit.MINUTES);
    }
}
