package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.agent.VmModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.*;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.service.GuestService;
import cn.roamblue.cloud.management.service.NetworkAllocateService;
import cn.roamblue.cloud.management.service.VncService;
import cn.roamblue.cloud.management.util.StoragePathUtil;
import cn.roamblue.cloud.management.util.TemplateType;
import cn.roamblue.cloud.management.util.VmStatus;
import cn.roamblue.cloud.management.util.VmType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class GuestServiceImpl extends AbstractVmService implements GuestService {
    @Autowired
    protected List<NetworkAllocateService> networkAllocateService;
    @Autowired
    private VncService vncService;

    @Override
    protected VmNetworkInfo allocateNetwork(NetworkInfo network, int vmId) {
        Optional<NetworkAllocateService> optional = networkAllocateService.stream().filter(t -> t.getType().equals(network.getType())).findAny();
        NetworkAllocateService allocateService = optional.orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "不支持的网络类型" + network.getType()));
        VmNetworkInfo vmNetworkInfo = allocateService.allocateGuestAddress(network.getId(), vmId);

        log.info("allocate guest network success,VM={} IP={} MAC={} Device={}", vmId, vmNetworkInfo.getIp(), vmNetworkInfo.getMac(), vmNetworkInfo.getDevice());
        return vmNetworkInfo;
    }

    @Override
    public VmInfo resume(int vmId) {
        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机不存在");
        }
        vm.setRemoveTime(null);
        vm.setVmStatus(VmStatus.STOPPED);
        vmMapper.updateById(vm);
        return this.initVmInfo(vm);

    }

    @Override
    public VmInfo changeCdRoom(int vmId, int iso) {

        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND,"虚拟机不存在");
        }
        String path = "";
        if (iso > 0) {
            TemplateInfo template = templateService.findTemplateById(iso);
            TemplateRefInfo templateRef = templateService.listTemplateRefByTemplateId(template.getId()).stream().findAny().get();
            StorageInfo templateStorage = storageService.findStorageById(templateRef.getStorageId());
            path = StoragePathUtil.getVolumePath(templateStorage.getTarget(), templateRef.getTarget());
        }
        vm.setVmIso(iso);
        vm.setLastUpdateTime(new Date());
        vmMapper.updateById(vm);
        if (vm.getVmStatus().equals(VmStatus.RUNNING)) {
            HostInfo host = this.hostService.findHostById(vm.getHostId());
            ResultUtil<Void> resultUtil = this.agentService.changeCdRoom(host.getUri(), vm.getVmName(), path);
            if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
            }

        }
        return this.initVmInfo(vm);
    }

    @Override
    public VolumeInfo attachDisk(int vmId, int volumeId) {
        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机不存在");
        }
        VolumeInfo volumeInfo = volumeService.attachVm(volumeId, vmId);
        vm.setLastUpdateTime(new Date());
        vmMapper.updateById(vm);

        if (vm.getVmStatus().equals(VmStatus.RUNNING)) {
            StorageEntity storage = this.storageMapper.selectById(volumeInfo.getStorageId());
            if (storage != null) {
                VmModel.Disk disk = VmModel.Disk.builder().path(StoragePathUtil.getVolumePath(storage.getStorageTarget(), volumeInfo.getTarget())).device(volumeInfo.getDevice()).build();

                HostInfo host = this.hostService.findHostById(vm.getHostId());
                this.agentService.attachDisk(host.getUri(), vm.getVmName(), disk, true);

            }
        }
        return volumeInfo;
    }

    @Override
    public VolumeInfo detachDisk(int vmId, int volumeId) {
        VolumeInfo volumeInfo = volumeService.detachVm(volumeId, vmId);

        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机不存在");
        }
        vm.setLastUpdateTime(new Date());
        vmMapper.updateById(vm);
        if (vm.getVmStatus().equals(VmStatus.RUNNING)) {
            StorageEntity storage = this.storageMapper.selectById(volumeInfo.getStorageId());
            if (storage != null) {
                VmModel.Disk disk = VmModel.Disk.builder().path(StoragePathUtil.getVolumePath(storage.getStorageTarget(), volumeInfo.getTarget())).device(volumeInfo.getDevice()).build();
                HostInfo host = this.hostService.findHostById(vm.getHostId());
                this.agentService.attachDisk(host.getUri(), vm.getVmName(), disk, false);
            }
        }
        return volumeInfo;

    }

    @Override
    public VmInfo modify(int vmId, String description, int calculationSchemeId, int groupId) {

        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机不存在");
        }
        vm.setVmDescription(description);
        vm.setCalculationSchemeId(calculationSchemeId);
        vm.setGroupId(groupId);
        vmMapper.updateById(vm);
        return this.initVmInfo(vm);

    }

    @Override
    public TemplateInfo createTemplate(int vmId, String name) {

        VmEntity vm = vmMapper.selectById(vmId);
        if (vm == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机不存在");
        }
        if (!vm.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED)) {
            throw new CodeException(ErrorCode.VM_NOT_STOP, "创建模版前请停止虚拟机");
        }
        List<VolumeInfo> volumeInfoList = this.volumeService.listVolumeByVmId(vmId);
        VolumeInfo volumeInfo = volumeInfoList.stream().filter(t -> t.getDevice() == 0).findAny().orElseThrow(() -> new CodeException(ErrorCode.VOLUME_NOT_READY, "创建模版磁盘未找到"));
        return this.volumeService.createTemplateById(volumeInfo.getId(), vm.getOsCategoryId(), name);

    }

    @Override
    public VmInfo reInstall(int vmId, int templateId,int storageId) {


        TemplateInfo template = templateService.findTemplateById(templateId);

        List<TemplateRefInfo> templateRefList = templateService.listTemplateRefByTemplateId(template.getId());
        if (templateRefList.isEmpty()) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "模版未就绪");
        }
        TemplateRefInfo templateRef = templateRefList.stream().findAny().get();


        StorageInfo templateStorage = storageService.findStorageById(templateRef.getStorageId());

        String parentVolumePath = null;
        if (!template.getType().equals(TemplateType.ISO)) {
            parentVolumePath = StoragePathUtil.getVolumePath(templateStorage.getTarget(), templateRef.getTarget());
        }
        String parentVolPath = parentVolumePath;
        VmEntity vm = this.stopVm(vmId, true);
        vm.setVmIso(0);
        vm.setTemplateId(templateId);
        vm.setOsCategoryId(template.getOsCategoryId());
        if (StringUtils.isEmpty(parentVolPath)) {
            vm.setVmIso(templateId);
        }
        VolumeInfo volumeInfo = this.volumeService.listVolumeByVmId(vmId).stream().filter(t -> t.getDevice() == 0).findFirst().orElseThrow(() -> new CodeException(ErrorCode.VOLUME_NOT_FOUND, "VM Root磁盘丢失"));
        this.volumeService.detachVm(volumeInfo.getId(), vmId);
        volumeInfo = this.volumeService.createVolume(vm.getClusterId(), parentVolPath, storageId, "ROOT-" + vm.getId(), volumeInfo.getCapacity() / 1024 / 1024 / 1024);

        this.volumeService.attachVm(volumeInfo.getId(), vmId);

        vmMapper.updateById(vm);
        return this.initVmInfo(vm);

    }

    @Override
    protected void onBeforeStart(VmEntity vm, HostEntity host) {

    }

    @Override
    protected void onAfterStart(VmEntity vm, HostEntity host) {
        this.vncService.register(vm.getClusterId(), vm.getId(), host.getHostIp(), vm.getVncPort(), vm.getVncPassword());
    }

    @Override
    protected void onStop(VmEntity vm) {
        this.vncService.unRegister(vm.getClusterId(), vm.getId());
    }

    @Override
    protected void onDestroy(VmEntity vm) {
        this.vncService.unRegister(vm.getClusterId(), vm.getId());
    }

    @Override
    public String getType() {
        return VmType.GUEST;
    }
}
