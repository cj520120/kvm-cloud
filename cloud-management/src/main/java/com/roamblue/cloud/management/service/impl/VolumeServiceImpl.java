package com.roamblue.cloud.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.roamblue.cloud.common.agent.VolumeModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.bean.VolumeInfo;
import com.roamblue.cloud.management.data.entity.*;
import com.roamblue.cloud.management.data.mapper.*;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.AllocateService;
import com.roamblue.cloud.management.service.VolumeService;
import com.roamblue.cloud.management.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VolumeServiceImpl implements VolumeService {
    @Autowired
    private VolumeMapper volumeMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private TemplateMapper templateRepository;
    @Autowired
    private TemplateRefMapper templateRefRepository;


    private void sort(List<VolumeInfo> list) {
        Collections.sort(list, (o1, o2) -> {
            int val1 = VolumeStatus.getCompareValue(o1.getStatus());
            int val2 = VolumeStatus.getCompareValue(o2.getStatus());
            int result = Integer.compare(val1, val2);
            if (result == 0) {
                result = Integer.compare(o1.getVmId(), o2.getVmId());
                if (result == 0) {
                    result = Integer.compare(o1.getDevice(), o2.getDevice());
                }
            }
            return result;
        });
    }

    @Override
    public List<VolumeInfo> listVolume() {
        List<VolumeEntity> entityList = volumeMapper.selectAll();
        List<VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        this.sort(list);
        return list;
    }

    @Override
    public List<VolumeInfo> search(int clusterId, int storageId, int vmId) {

        QueryWrapper<VolumeEntity> wrapper = new QueryWrapper<>();
        if (clusterId > 0) {
            wrapper.eq("cluster_id", clusterId);
        }
        if (storageId > 0) {
            wrapper.eq("storage_id", storageId);
        }
        if (vmId >= 0) {
            wrapper.eq("vm_id", vmId);
        }
        wrapper.orderBy(true, false, "vm_device", "vm_id");
        List<VolumeEntity> entityList = volumeMapper.selectList(wrapper);
        List<VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        this.sort(list);
        return list;
    }


    @Override
    public List<VolumeInfo> listVolumeByVmId(int vmId) {

        List<VolumeEntity> entityList = volumeMapper.findByVmId(vmId);
        List<VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        return list;
    }


    @Override
    public VolumeInfo findVolumeById(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        VolumeInfo info = init(entity);
        return info;
    }

    @Override
    public VolumeInfo createVolume(int clusterId, String parentVolumePath, int storageId, String name, long size) {

        ClusterEntity clusterEntity = this.clusterMapper.selectById(clusterId);
        if (clusterEntity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "集群不存在");
        }
        StorageEntity storageEntity = this.allocateService.allocateStorage(clusterId, 0, size);

        HostEntity host = this.allocateService.allocateHost(clusterId, 0, 0, 0);
        String target = UUID.randomUUID().toString().replace("-", "");
        ResultUtil<VolumeModel> createResultUtil = this.agentService.createVolume(host.getHostUri(), storageEntity.getStorageTarget(), target, parentVolumePath, size);
        if (createResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(createResultUtil.getCode(), createResultUtil.getMessage());
        }
        VolumeModel kvmVolumeInfo = createResultUtil.getData();
        VolumeEntity volumeEntity = VolumeEntity.builder()
                .clusterId(clusterId)
                .storageId(storageEntity.getId())
                .vmId(0)
                .vmDevice(0)
                .volumeName(name)
                .volumeStatus(com.roamblue.cloud.management.util.VolumeStatus.READY)
                .volumeTarget(target)
                .volumeCapacity(size)
                .volumeAllocation(kvmVolumeInfo.getAllocation())
                .createTime(new Date())
                .build();
        volumeEntity.setVolumeAllocation(kvmVolumeInfo.getAllocation());
        volumeEntity.setVolumeCapacity(kvmVolumeInfo.getCapacity());
        volumeEntity.setVolumeStatus(com.roamblue.cloud.management.util.VolumeStatus.READY);
        volumeMapper.insert(volumeEntity);
        VolumeInfo info = init(volumeEntity);
        log.info("创建磁盘信息成功:info={}", kvmVolumeInfo);
        return info;
    }

    @Override
    public VolumeInfo attachVm(int volumeId, int vmId) {

        VolumeEntity entity = volumeMapper.selectById(volumeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (entity.getVmId() > 0) {
            throw new CodeException(ErrorCode.VOLUME_ATTACH_ERROR, "磁盘卷已挂载");
        }
        StorageEntity storageEntity = this.storageMapper.selectById(entity.getStorageId());
        if (storageEntity == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        List<Integer> deviceIds = volumeMapper.findByVmId(vmId).stream().map(VolumeEntity::getVmDevice).collect(Collectors.toList());
        int device = 0;
        while (deviceIds.contains(device)) {
            device++;
        }
        entity.setVmDevice(device);
        entity.setVmId(vmId);
        volumeMapper.updateById(entity);
        log.info("挂载磁盘信息成功:volumeId={} vmId={} device={}", volumeId, vmId, device);
        return this.init(entity);
    }

    @Override
    public VolumeInfo detachVm(int volumeId, int vmId) {

        VolumeEntity entity = volumeMapper.selectById(volumeId);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (entity.getVmId() > 0 && entity.getVmId() != vmId) {
            throw new CodeException(ErrorCode.VOLUME_ATTACH_ERROR, "磁盘卷挂载到其他虚拟机");
        }
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        entity.setVmId(0);
        volumeMapper.updateById(entity);
        log.info("取消挂载磁盘信息成功:volumeId={} vmId={}", volumeId, vmId);
        return this.init(entity);

    }

    @Override
    public VolumeInfo resize(int id, long size) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (entity.getVmId() > 0) {
            VmEntity vm = this.vmMapper.selectById(entity.getVmId());
            if (vm != null && !vm.getVmStatus().equalsIgnoreCase(InstanceStatus.STOPPED)) {
                throw new CodeException(ErrorCode.VM_NOT_STOP, "磁盘调整时虚拟机必须处于停止状态");
            }
        }
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        size = entity.getVolumeCapacity() + size * 1024 * 1024 * 1024;
        HostEntity hostEntity = this.allocateService.allocateHost(entity.getClusterId(), 0, 0, 0);

        ResultUtil<VolumeModel> resizeResultUtil = this.agentService.resize(hostEntity.getHostUri(), storage.getStorageTarget(), entity.getVolumeTarget(), size);
        if (resizeResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resizeResultUtil.getCode(), resizeResultUtil.getMessage());
        }
        VolumeModel kvmVolumeInfo = resizeResultUtil.getData();
        entity.setVolumeAllocation(kvmVolumeInfo.getAllocation());
        entity.setVolumeCapacity(kvmVolumeInfo.getCapacity());
        volumeMapper.updateById(entity);
        log.info("扩容磁盘成功:volumeId={} size={}", id, size);
        return this.init(entity);
    }

    @Override
    public VolumeInfo resume(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        entity.setRemoveTime(null);
        entity.setVolumeStatus(com.roamblue.cloud.management.util.VolumeStatus.READY);
        volumeMapper.updateById(entity);
        log.info("恢复磁盘成功:volumeId={}", id);
        return this.init(entity);
    }


    @Override
    public void destroyByVmId(int vmId) {
        List<VolumeEntity> list = volumeMapper.findByVmId(vmId);
        for (VolumeEntity volume : list) {
            StorageEntity storage = this.storageMapper.selectById(volume.getStorageId());
            if (storage != null) {
                HostEntity hostEntity = this.allocateService.allocateHost(volume.getClusterId(), 0, 0, 0);
                this.agentService.destroyVolume(hostEntity.getHostUri(), storage.getStorageTarget(), volume.getVolumeTarget());
            }
            volumeMapper.deleteById(volume.getId());

        }
        log.info("释放主机磁盘成功:vmId={}", vmId);

    }

    @Override
    public TemplateInfo createTemplateById(int id, int osCategoryId, String name) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (!entity.getVolumeStatus().equals(com.roamblue.cloud.management.util.VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘卷未就绪");
        }
        entity.setVolumeStatus(com.roamblue.cloud.management.util.VolumeStatus.TEMPLATE);
        volumeMapper.updateById(entity);
        try {
            StorageEntity sourceStorageEntity = this.storageMapper.selectById(entity.getStorageId());
            if (!sourceStorageEntity.getStorageStatus().equalsIgnoreCase(StorageStatus.READY)) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "磁盘卷所在存储未就绪");
            }
            StorageEntity toStorageEntity = this.allocateService.allocateStorage(entity.getClusterId(), 0, entity.getVolumeAllocation());
            HostEntity hostEntity = this.allocateService.allocateHost(entity.getClusterId(), 0, 0, 0);
            String sourceStorage = sourceStorageEntity.getStorageTarget();
            String targetStorage = toStorageEntity.getStorageTarget();
            String sourceVolume = entity.getVolumeTarget();
            String targetVolume = UUID.randomUUID().toString().replace("-", "");
            String targetPath = "/mnt/" + targetStorage + "/" + targetVolume;

            ResultUtil<VolumeModel> cloneResultUtil = this.agentService.cloneVolume(hostEntity.getHostUri(), sourceStorage, sourceVolume, targetStorage, targetVolume, targetPath);
            if (cloneResultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(cloneResultUtil.getCode(), cloneResultUtil.getMessage());
            }
            VolumeModel cloudVolumeInfo = cloneResultUtil.getData();
            TemplateEntity templateEntity = TemplateEntity.builder()
                    .clusterId(entity.getClusterId()).templateName(name)
                    .templateSize(cloudVolumeInfo.getAllocation())
                    .templateStatus(TemplateStatus.READY)
                    .templateType(TemplateType.DISK)
                    .osCategoryId(osCategoryId)
                    .templateUri("")
                    .createTime(new Date())
                    .build();
            this.templateRepository.insert(templateEntity);
            TemplateRefEntity refEntity = TemplateRefEntity.builder()
                    .clusterId(entity.getClusterId())
                    .createTime(new Date())
                    .storageId(toStorageEntity.getId())
                    .templateTarget(targetVolume)
                    .templateId(templateEntity.getId())
                    .templateStatus(TemplateStatus.READY)
                    .build();
            this.templateRefRepository.insert(refEntity);
            TemplateInfo templateInfo = TemplateInfo.builder().clusterId(entity.getClusterId())
                    .id(templateEntity.getId())
                    .name(templateEntity.getTemplateName())
                    .uri(templateEntity.getTemplateUri())
                    .type(templateEntity.getTemplateType())
                    .createTime(templateEntity.getCreateTime())
                    .status(TemplateStatus.READY)
                    .build();
            return templateInfo;

        } finally {
            entity.setVolumeStatus(com.roamblue.cloud.management.util.VolumeStatus.READY);
            volumeMapper.updateById(entity);
        }

    }

    @Override
    public VolumeInfo destroyVolumeById(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (!entity.getVolumeStatus().equals(com.roamblue.cloud.management.util.VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘卷未就绪");
        }
        entity.setRemoveTime(new Date());
        entity.setVolumeStatus(com.roamblue.cloud.management.util.VolumeStatus.DESTROY);
        log.info("销毁磁盘成功:id={}", id);
        volumeMapper.updateById(entity);
        return this.init(entity);
    }

    private VolumeInfo init(VolumeEntity entity) {
        return VolumeInfo.builder()
                .id(entity.getId())
                .clusterId(entity.getClusterId())
                .storageId(entity.getStorageId())
                .vmId(entity.getVmId())
                .device(entity.getVmDevice())
                .target(entity.getVolumeTarget())
                .name(entity.getVolumeName())
                .capacity(entity.getVolumeCapacity())
                .status(entity.getVolumeStatus())
                .allocation(entity.getVolumeAllocation())
                .createTime(entity.getCreateTime())
                .build();
    }
}
