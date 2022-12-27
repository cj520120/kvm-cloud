package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.TemplateInfo;
import cn.roamblue.cloud.management.bean.VolumeSnapshot;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.AllocateService;
import cn.roamblue.cloud.management.service.VolumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class VolumeServiceImpl extends AbstractService implements VolumeService {
    @Autowired
    private VolumeMapper volumeMapper; 
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


    private void sort(List<cn.roamblue.cloud.management.bean.VolumeInfo> list) {
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
    public List<cn.roamblue.cloud.management.bean.VolumeInfo> listVolume() {
        List<VolumeEntity> entityList = volumeMapper.selectAll();
        List<cn.roamblue.cloud.management.bean.VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        this.sort(list);
        return list;
    }

    @Override
    public List<cn.roamblue.cloud.management.bean.VolumeInfo> search(int clusterId, int storageId, int vmId) {

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
        List<cn.roamblue.cloud.management.bean.VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        this.sort(list);
        return list;
    }


    @Override
    public List<cn.roamblue.cloud.management.bean.VolumeInfo> listVolumeByVmId(int vmId) {

        List<VolumeEntity> entityList = volumeMapper.findByVmId(vmId);
        List<cn.roamblue.cloud.management.bean.VolumeInfo> list = BeanConverter.convert(entityList, this::init);
        return list;
    }


    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo findVolumeById(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        cn.roamblue.cloud.management.bean.VolumeInfo info = init(entity);
        return info;
    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo createVolume(int clusterId, String parentVolumePath, int storageId, String name, long size) {

        ClusterEntity clusterEntity = this.clusterMapper.selectById(clusterId);
        if (clusterEntity == null) {
            throw new CodeException(ErrorCode.CLUSTER_NOT_FOUND, "集群不存在");
        }
        StorageEntity storageEntity = this.allocateService.allocateStorage(clusterId, storageId, size);

        HostEntity host = this.allocateService.allocateHost(clusterId, 0, 0, 0);
        String target = UUID.randomUUID().toString().replace("-", "");
        ResultUtil<VolumeInfo> createResultUtil = this.agentService.createVolume(host.getHostUri(), storageEntity.getStorageTarget(), target, parentVolumePath, size);
        if (createResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(createResultUtil.getCode(), createResultUtil.getMessage());
        }
        VolumeInfo kvmVolumeInfo = createResultUtil.getData();
        VolumeEntity volumeEntity = VolumeEntity.builder()
                .clusterId(clusterId)
                .storageId(storageEntity.getId())
                .vmId(0)
                .vmDevice(0)
                .volumeName(name)
                .volumeStatus(VolumeStatus.READY)
                .volumeTarget(target)
                .volumeCapacity(size)
                .volumeAllocation(kvmVolumeInfo.getAllocation())
                .createTime(new Date())
                .build();
        volumeEntity.setVolumeAllocation(kvmVolumeInfo.getAllocation());
        volumeEntity.setVolumeCapacity(kvmVolumeInfo.getCapacity());
        volumeEntity.setVolumeStatus(VolumeStatus.READY);
        volumeMapper.insert(volumeEntity);
        cn.roamblue.cloud.management.bean.VolumeInfo info = init(volumeEntity);
        log.info("create volume success:info={}", kvmVolumeInfo);
        return info;
    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo attachVm(int volumeId, int vmId) {

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
        log.info("attach volume success:volumeId={} vmId={} device={}", volumeId, vmId, device);
        return this.init(entity);
    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo detachVm(int volumeId, int vmId) {

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
        log.info("detach volume success.volumeId={} vmId={}", volumeId, vmId);
        return this.init(entity);

    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo resize(int id, long size) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (entity.getVmId() > 0) {
            VmEntity vm = this.vmMapper.selectById(entity.getVmId());
            if (vm != null && !vm.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED)) {
                throw new CodeException(ErrorCode.VM_NOT_STOP, "虚拟机未停止");
            }
        }
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        size = entity.getVolumeCapacity() + size * 1024 * 1024 * 1024;
        HostEntity hostEntity = this.allocateService.allocateHost(entity.getClusterId(), 0, 0, 0);

        ResultUtil<VolumeInfo> resizeResultUtil = this.agentService.resize(hostEntity.getHostUri(), storage.getStorageTarget(), entity.getVolumeTarget(), size);
        if (resizeResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resizeResultUtil.getCode(), resizeResultUtil.getMessage());
        }
        VolumeInfo kvmVolumeInfo = resizeResultUtil.getData();
        entity.setVolumeAllocation(kvmVolumeInfo.getAllocation());
        entity.setVolumeCapacity(kvmVolumeInfo.getCapacity());
        volumeMapper.updateById(entity);
        log.info("resize volume success.volumeId={} size={}", id, size);
        return this.init(entity);
    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo resume(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        entity.setRemoveTime(null);
        entity.setVolumeStatus(VolumeStatus.READY);
        volumeMapper.updateById(entity);
        log.info("resume volume success:volumeId={}", id);
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
        log.info("free volume success:vmId={}", vmId);

    }

    @Override
    public TemplateInfo createTemplateById(int id, int osCategoryId, String name) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (!entity.getVolumeStatus().equals(VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘卷未就绪");
        }
        entity.setVolumeStatus(VolumeStatus.TEMPLATE);
        volumeMapper.updateById(entity);
        try {
            StorageEntity sourceStorageEntity = this.storageMapper.selectById(entity.getStorageId());
            if (!sourceStorageEntity.getStorageStatus().equalsIgnoreCase(StorageStatus.READY)) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            StorageEntity toStorageEntity = this.allocateService.allocateStorage(entity.getClusterId(), 0, entity.getVolumeAllocation());
            HostEntity hostEntity = this.allocateService.allocateHost(entity.getClusterId(), 0, 0, 0);
            String sourceStorage = sourceStorageEntity.getStorageTarget();
            String targetStorage = toStorageEntity.getStorageTarget();
            String sourceVolume = entity.getVolumeTarget();
            String targetVolume = UUID.randomUUID().toString().replace("-", "");
            String targetPath = StoragePathUtil.getVolumePath(targetStorage, targetVolume);

            ResultUtil<VolumeInfo> cloneResultUtil = this.agentService.cloneVolume(hostEntity.getHostUri(), sourceStorage, sourceVolume, targetStorage, targetVolume, targetPath);
            if (cloneResultUtil.getCode() != ErrorCode.SUCCESS) {
                throw new CodeException(cloneResultUtil.getCode(), cloneResultUtil.getMessage());
            }
            VolumeInfo cloudVolumeInfo = cloneResultUtil.getData();
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
            entity.setVolumeStatus(VolumeStatus.READY);
            volumeMapper.updateById(entity);
        }

    }

    @Override
    public cn.roamblue.cloud.management.bean.VolumeInfo destroyVolumeById(int id) {

        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        if (!entity.getVolumeStatus().equals(VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘卷未就绪");
        }
        entity.setRemoveTime(new Date());
        entity.setVolumeStatus(VolumeStatus.DESTROY);
        log.info("销毁磁盘成功:id={}", id);
        volumeMapper.updateById(entity);
        return this.init(entity);
    }

    @Override
    public List<VolumeSnapshot> listVolumeSnapshot(int id) {
        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        VmEntity vm = vmMapper.selectById(entity.getVmId());
        HostEntity host = this.allocateService.allocateHost(storage.getClusterId(), 0, 0, 0);
        ResultUtil<List<VolumeSnapshot>> resultUtil = this.agentService.listVolumeSnapshot(host.getHostUri(), vm == null ? "" : vm.getVmName(), storage.getStorageTarget(), entity.getVolumeTarget());
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
        return resultUtil.getData();
    }

    @Override
    public VolumeSnapshot createVolumeSnapshot(int id) {
        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        VmEntity vm = vmMapper.selectById(entity.getVmId());
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        HostEntity host = this.allocateService.allocateHost(storage.getClusterId(), vm == null ? 0 : vm.getHostId(), 0, 0);
        String name = "volume-snapshot-" + System.currentTimeMillis();
        ResultUtil<VolumeSnapshot> resultUtil = this.agentService.createVolumeSnapshot(host.getHostUri(), vm == null ? "" : vm.getVmName(), storage.getStorageTarget(), entity.getVolumeTarget(), name);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
        return resultUtil.getData();
    }

    @Override
    public void revertVolumeSnapshot(int id, String name) {
        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        VmEntity vm = vmMapper.selectById(entity.getVmId());
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        HostEntity host = this.allocateService.allocateHost(storage.getClusterId(), vm == null ? 0 : vm.getHostId(), 0, 0);
        ResultUtil<Void> resultUtil = this.agentService.revertVolumeSnapshot(host.getHostUri(), vm == null ? "" : vm.getVmName(), storage.getStorageTarget(), entity.getVolumeTarget(), name);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
    }

    @Override
    public void deleteVolumeSnapshot(int id, String name) {
        VolumeEntity entity = volumeMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘卷不存在");
        }
        VmEntity vm = vmMapper.selectById(entity.getVmId());
        StorageEntity storage = this.storageMapper.selectById(entity.getStorageId());
        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储不存在");
        }
        HostEntity host = this.allocateService.allocateHost(storage.getClusterId(), vm == null ? 0 : vm.getHostId(), 0, 0);
        ResultUtil<Void> resultUtil = this.agentService.deleteVolumeSnapshot(host.getHostUri(), vm == null ? "" : vm.getVmName(), storage.getStorageTarget(), entity.getVolumeTarget(), name);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
    }

    private cn.roamblue.cloud.management.bean.VolumeInfo init(VolumeEntity entity) {
        return cn.roamblue.cloud.management.bean.VolumeInfo.builder()
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
