package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.util.BeanConverter;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.NameUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class VolumeService extends AbstractService {

    @Autowired
    private AllocateService allocateService;
    @Autowired
    private TemplateService templateService;


    private VolumeEntity findAndUpdateVolumeStatus(int volumeId, int status) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        if (!Objects.equals(volume.getStatus(), Constant.VolumeStatus.READY)) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
        volume.setStatus(status);
        this.volumeMapper.updateById(volume);
        return volume;
    }


    public ResultUtil<List<VolumeModel>> listGuestVolumes(int guestId) {
        List<VolumeEntity> diskList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guestId));
        List<VolumeModel> models = diskList.stream().map(this::initVolume).sorted((o1, o2) -> {
            if (o1.getStatus() == o2.getStatus()) {
                return Integer.compare(o1.getVolumeId(), o2.getVolumeId());
            }
            if (o1.getStatus() == Constant.VolumeStatus.READY) {
                return -1;
            }
            if (o2.getStatus() == Constant.VolumeStatus.READY) {
                return 1;
            }
            return Integer.compare(o1.getStatus(), o2.getStatus());
        }).sorted(Comparator.comparingInt(SimpleVolumeModel::getDeviceId)).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<Page<SimpleVolumeModel>> search(Integer storageId, Integer status, Integer templateId, String volumeType, String keyword, int no, int size) {
        QueryWrapper<VolumeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(storageId != null, VolumeEntity.STORAGE_ID, storageId);
        queryWrapper.eq(templateId != null, VolumeEntity.TEMPLATE_ID, templateId);
        queryWrapper.eq(status != null, VolumeEntity.VOLUME_STATUS, status);
        queryWrapper.eq(!ObjectUtils.isEmpty(volumeType), VolumeEntity.VOLUME_TYPE, volumeType);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<VolumeEntity> wrapper = o;
                wrapper.like(VolumeEntity.VOLUME_NAME, condition)
                        .or().like(VolumeEntity.VOLUME_DESCRIPTION, condition);
            });
        }
        int nCount = Math.toIntExact(this.volumeMapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(queryWrapper);
        List<SimpleVolumeModel> models = this.initSimpleVolumeList(volumeList);
        Page<SimpleVolumeModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);

    }

    public ResultUtil<List<SimpleVolumeModel>> listVolumes() {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<>());
        List<SimpleVolumeModel> models = this.initSimpleVolumeList(volumeList);
        return ResultUtil.success(models);
    }

    public ResultUtil<List<SimpleVolumeModel>> listNoAttachVolumes(int guestId) {
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, 0));
        int allowHostId = this.getGuestMustStartHostId(guestId);
        List<SimpleVolumeModel> models = new ArrayList<>();
        for (VolumeEntity volume : volumeList) {
            if (!Objects.equals(volume.getStatus(), Constant.VolumeStatus.READY)) {
                continue;
            }
            if (allowHostId == 0 || volume.getHostId() == 0 || volume.getHostId() == allowHostId) {
                models.add(BeanConverter.convert(volume, SimpleVolumeModel.class));
            }
        }
        return ResultUtil.success(models);
    }

    public ResultUtil<VolumeModel> getVolumeInfo(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            return ResultUtil.error(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        return ResultUtil.success(this.initVolume(volume));
    }


    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> createVolume(String description, int storageId, int templateId, long volumeSize) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘备注");
        }
        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME, storageId);
        String volumeType = getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .templateId(0)
                .hostId(storage.getHostId())
                .description(description)
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volumeSize)
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .deviceDriver("")
                .guestId(0)
                .deviceId(0)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(volume);
        BaseOperateParam operateParam = CreateVolumeOperate.builder().id(UUID.randomUUID().toString()).title("创建磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).templateId(templateId).build();
        operateTask.addTask(operateParam);
        VolumeModel model = this.initVolume(volume);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<CloneModel> cloneVolume(String description, int sourceVolumeId, int storageId) {

        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME, storageId);
        String volumeType = getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.CLONE);
        if (volume.getHostId() > 0 && storage.getHostId() > 0 && !Objects.equals(volume.getHostId(), storage.getHostId())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "跨宿主机本地磁盘无法完成克隆，如需进行操作，请先迁移到共享存储，然后再进行克隆");
        }
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(Constant.VolumeStatus.CLONE);
        this.volumeMapper.updateById(volume);
        VolumeEntity cloneVolume = VolumeEntity.builder()
                .storageId(storage.getStorageId())
                .hostId(storage.getHostId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .description(description)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(cloneVolume);
        BaseOperateParam operateParam = CloneVolumeOperate.builder().id(UUID.randomUUID().toString())
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(cloneVolume.getVolumeId())
                .title("克隆磁盘[" + volume.getName() + "]")
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel clone = this.initVolume(cloneVolume);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(cloneVolume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(CloneModel.builder().source(source).clone(clone).build());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> resizeVolume(int volumeId, long size) { 
        if (size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入新增的磁盘大小");
        }
        VolumeEntity volume = this.findAndUpdateVolumeStatus(volumeId, Constant.VolumeStatus.RESIZE);
        volume.setCapacity(volume.getCapacity() + size);
        volume.setStatus(Constant.VolumeStatus.RESIZE);
        this.volumeMapper.updateById(volume);
        BaseOperateParam operateParam = ResizeVolumeOperate.builder().id(UUID.randomUUID().toString())
                .title("更改磁盘大小[" + volume.getName() + "]")
                .volumeId(volume.getVolumeId())
                .size(volume.getCapacity())
                .build();
        operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initVolume(volume));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<MigrateModel> migrateVolume(int sourceVolumeId, int storageId) {
        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME, storageId);
        String volumeType =getVolumeType(storage);
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = this.findAndUpdateVolumeStatus(sourceVolumeId, Constant.VolumeStatus.MIGRATE);
        if (volume.getHostId() > 0 && storage.getHostId() > 0 && !Objects.equals(volume.getHostId(), storage.getHostId())) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "跨宿主机本地磁盘无法完成迁移，如需进行操作，请先迁移到共享存储，然后再进行迁移");
        }
        GuestEntity guest = this.getVolumeGuest(sourceVolumeId);
        if (guest != null) {
            switch (guest.getStatus()) {
                case Constant.GuestStatus.STOP:
                case Constant.GuestStatus.ERROR:
                    break;
                default:
                    throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前磁盘所在虚拟机正在运行,请关机后重试");
            }
        }
        volume.setStatus(Constant.VolumeStatus.MIGRATE);
        this.volumeMapper.updateById(volume);
        VolumeEntity migrateVolume = VolumeEntity.builder()
                .description(volume.getDescription())
                .storageId(storage.getStorageId())
                .hostId(storage.getHostId())
                .templateId(volume.getTemplateId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .capacity(volume.getCapacity())
                .allocation(0L)
                .status(Constant.VolumeStatus.CREATING)
                .deviceId(0)
                .deviceDriver("")
                .guestId(0)
                .createTime(new Date())
                .build();
        this.volumeMapper.insert(migrateVolume);
        BaseOperateParam operateParam = MigrateVolumeOperate.builder().id(UUID.randomUUID().toString())
                .title("迁移磁盘[" + volume.getName() + "]")
                .sourceVolumeId(volume.getVolumeId())
                .targetVolumeId(migrateVolume.getVolumeId())
                .build();
        operateTask.addTask(operateParam);
        VolumeModel source = this.initVolume(volume);
        VolumeModel migrate = this.initVolume(migrateVolume);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(migrate.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(MigrateModel.builder().source(source).migrate(migrate).build());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<VolumeModel>> batchDestroyVolume(List<Integer> volumeIds) {
        List<VolumeModel> models = new ArrayList<>(volumeIds.size());
        for (Integer volumeId : volumeIds) {
            try {
                models.add(this.destroyVolume(volumeId).getData());
            } catch (Exception ignored) {

            }
        }
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> createVolumeTemplate(int volumeId, String name) {
        ResultUtil<TemplateModel> resultUtil = this.templateService.createVolumeTemplate(volumeId, name);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return ResultUtil.error(resultUtil.getCode(), resultUtil.getMessage());
        }
        return this.getVolumeInfo(volumeId);
    }
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> destroyVolume(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            return ResultUtil.error(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        switch (volume.getStatus()) {
            case Constant.VolumeStatus.ERROR:
            case Constant.VolumeStatus.READY: {
                if (volume.getGuestId() > 0) {
                    throw new CodeException(ErrorCode.GUEST_VOLUME_HAS_ATTACH_ERROR, "当前磁盘被系统挂载");
                }
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volumeId).build();
                operateTask.addTask(operate, volume.getStatus() == Constant.VolumeStatus.ERROR ? 0 : configService.getConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE));
                VolumeModel source = this.initVolume(volume);
                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(source);
            }
            case Constant.VolumeStatus.DESTROY: {
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volumeId).build();
                operateTask.addTask(operate, 0);
                VolumeModel source = this.initVolume(volume);
                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(source);
            }
            default:
                throw new CodeException(ErrorCode.VOLUME_NOT_READY, "磁盘当前状态未就绪");
        }
    }

}

