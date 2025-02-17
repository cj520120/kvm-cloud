package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestPasswordMapper;
import cn.chenjun.cloud.management.data.mapper.MetaMapper;
import cn.chenjun.cloud.management.model.AttachGuestNetworkModel;
import cn.chenjun.cloud.management.model.AttachGuestVolumeModel;
import cn.chenjun.cloud.management.model.GuestModel;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.GuestNameUtil;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class GuestService extends AbstractService {


    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    private AllocateService allocateService;

    @Autowired
    private GuestPasswordMapper guestPasswordMapper;
    @Autowired
    private MetaMapper metaMapper;

    private void initGuestMetaData(int guestId, Map<String, String> metaData, Map<String, String> userData) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        Map<String, String> metaDataMap = new HashMap<>(4);
        String hostname = "VM-" + guest.getGuestIp().replace(".", "-");
        metaDataMap.put("hostname", hostname);
        metaDataMap.put("local-hostname", hostname);
        metaDataMap.put("instance-id", guest.getName());
        metaDataMap.putAll(metaData);
        this.metaMapper.delete(new QueryWrapper<MetaDataEntity>().eq(MetaDataEntity.GUEST_ID, guestId));
        for (Map.Entry<String, String> entry : metaDataMap.entrySet()) {
            MetaDataEntity metaDataEntity = MetaDataEntity.builder().guestId(guest.getGuestId()).metaKey(entry.getKey()).metaValue(entry.getValue()).build();
            this.metaMapper.insert(metaDataEntity);
        }
        String password = userData.get("password");
        this.guestPasswordMapper.deleteById(guestId);
        if (!StringUtils.isEmpty(password)) {
            SymmetricCryptoUtil util = SymmetricCryptoUtil.build();
            GuestPasswordEntity entity = GuestPasswordEntity.builder()
                    .guestId(guest.getGuestId())
                    .ivKey(util.getIvKey())
                    .encodeKey(util.getEncodeKey())
                    .password(util.encrypt(password))
                    .build();
            this.guestPasswordMapper.insert(entity);
        }
        String sshId = userData.getOrDefault("sshId","");
        if (!StringUtils.isEmpty(sshId)) {
            this.guestSshMapper.delete(new QueryWrapper<GuestSshEntity>().eq(GuestSshEntity.GUEST_ID, guestId));
            this.guestSshMapper.insert(GuestSshEntity.builder().sshId(NumberUtil.parseInt(sshId)).guestId(guestId).build());
        }
    }

    private void checkSystemComponentComplete(int networkId) {
        if (!this.checkComponentComplete(networkId, Constant.ComponentType.ROUTE)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网络服务未初始化完成,请稍后重试");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<>());
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());

        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listUserGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq(GuestEntity.GUEST_TYPE, Constant.GuestType.USER));
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).sorted((o1, o2) -> {
            if (o1.getStatus() == o2.getStatus()) {
                return Integer.compare(o1.getGuestId(), o2.getGuestId());
            }
            if (o1.getStatus() == Constant.GuestStatus.RUNNING) {
                return -1;
            }
            if (o2.getStatus() == Constant.GuestStatus.RUNNING) {
                return 1;
            }
            return Integer.compare(o1.getStatus(), o2.getStatus());
        }).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listSystemGuests(int networkId) {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq(GuestEntity.NETWORK_ID, networkId).eq(GuestEntity.GUEST_TYPE, Constant.GuestType.COMPONENT));
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return ResultUtil.error(ErrorCode.GUEST_NOT_FOUND, "虚拟机不存在");
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> createGuest(int groupId, String description, int systemCategory, int bootstrapType, String busType
            , int hostId, int schemeId, int networkId, String networkDeviceType,
                                              int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                              int storageId, String volumeType, Map<String, String> metaData, Map<String, String> userData, long size) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的描述信息");
        }
        if (StringUtils.isEmpty(busType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择总线方式");
        }
        if (StringUtils.isEmpty(networkDeviceType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择网卡驱动");
        }
        if (isoTemplateId <= 0 && diskTemplateId <= 0 && snapshotVolumeId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        if (isoTemplateId > 0 && size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
        this.checkSystemComponentComplete(networkId);
        SchemeEntity scheme = this.schemeMapper.selectById(schemeId);
        GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
        String uid = UUID.randomUUID().toString().replace("-", "");
        GuestEntity guest = GuestEntity.builder()
                .groupId(groupId)
                .name(GuestNameUtil.getName())
                .description(description)
                .systemCategory(systemCategory)
                .bootstrapType(bootstrapType)
                .busType(busType)
                .cpu(scheme.getCpu())
                .speed(scheme.getSpeed())
                .memory(scheme.getMemory())
                .cdRoom(isoTemplateId)
                .hostId(0)
                .lastHostId(0)
                .schemeId(schemeId)
                .guestIp(guestNetwork.getIp())
                .otherId(0)
                .networkId(networkId)
                .type(Constant.GuestType.USER)
                .status(Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);

        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(networkDeviceType);
        guestNetwork.setAllocateId(guest.getGuestId());
        guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
        this.guestNetworkMapper.updateById(guestNetwork);
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
            createGuest(hostId, diskTemplateId, snapshotVolumeId, volumeType, metaData, userData, size, uid, guest, storage);
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volumeId));
            if (guestDisk != null) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
            }
            guestDisk = GuestDiskEntity.builder()
                    .volumeId(volumeId)
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(hostId)
                    .taskId(uid)
                    .title("启动客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        }
        this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    private void createGuest(int hostId, int diskTemplateId, int snapshotVolumeId, String volumeType, Map<String, String> metaData, Map<String, String> userData, long size, String uid, GuestEntity guest, StorageEntity storage) {
        GuestDiskEntity guestDisk = createGuestVolume(diskTemplateId, volumeType, size, uid, guest, storage);
        this.initGuestMetaData(guest.getGuestId(), metaData, userData);
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .snapshotVolumeId(snapshotVolumeId)
                .templateId(diskTemplateId)
                .volumeId(guestDisk.getVolumeId())
                .start(true)
                .hostId(hostId)
                .taskId(uid)
                .title("创建客户机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);


        this.eventService.publish(NotifyData.<Void>builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());
    }

    private GuestDiskEntity createGuestVolume(int diskTemplateId, String volumeType, long size, String uid, GuestEntity guest, StorageEntity storage) {
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(size)
                .storageId(storage.getStorageId())
                .name(uid)
                .path(storage.getMountPath() + "/" + uid)
                .type(volumeType)
                .templateId(diskTemplateId)
                .allocation(0L)
                .capacity(size)
                .backingPath("")
                .status(Constant.VolumeStatus.CREATING)
                .build();
        this.volumeMapper.insert(volume);
        GuestDiskEntity guestDisk = GuestDiskEntity.builder()
                .volumeId(volume.getVolumeId())
                .guestId(guest.getGuestId())
                .deviceId(0)
                .build();
        this.guestDiskMapper.insert(guestDisk);

        this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return guestDisk;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reInstall(int guestId, int systemCategory, int bootstrapType, Map<String, String> metaData, Map<String, String> userData, int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                            int storageId, String volumeType, long size) {

        if (isoTemplateId <= 0 && diskTemplateId <= 0 && snapshotVolumeId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() != Constant.GuestStatus.STOP) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "只能对关机状态的主机进行重装操作");
        }
        this.checkSystemComponentComplete(guest.getNetworkId());
        String uid = UUID.randomUUID().toString().replace("-", "");
        guest.setCdRoom(isoTemplateId);
        guest.setSystemCategory(systemCategory);
        guest.setBootstrapType(bootstrapType);
        this.initGuestMetaData(guestId, metaData, userData);
        this.guestDiskMapper.delete(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guestId).eq(GuestDiskEntity.DEVICE_ID, 0));
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
            GuestDiskEntity guestDisk = createGuestVolume(diskTemplateId, volumeType, size, uid, guest, storage);
            guest.setStatus(Constant.GuestStatus.CREATING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .snapshotVolumeId(snapshotVolumeId)
                    .templateId(diskTemplateId)
                    .volumeId(guestDisk.getVolumeId())
                    .taskId(uid)
                    .start(true)
                    .title("重装客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volumeId));
            if (guestDisk != null) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
            }
            guestDisk = GuestDiskEntity.builder()
                    .volumeId(volumeId)
                    .guestId(guest.getGuestId())
                    .deviceId(0)
                    .build();
            this.guestDiskMapper.insert(guestDisk);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(0)
                    .taskId(uid)
                    .title("重装客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        }
        this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> batchStart(List<Integer> guestIds) {
        List<GuestModel> models = new ArrayList<>(guestIds.size());
        for (Integer guestId : guestIds) {
            try {
                GuestModel model = this.start(guestId, 0).getData();
                models.add(model);
            } catch (Exception ignored) {

            }
        }
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> batchStop(List<Integer> guestIds) {
        List<GuestModel> models = new ArrayList<>(guestIds.size());
        for (Integer guestId : guestIds) {
            try {
                GuestModel model = this.shutdown(guestId, false).getData();
                models.add(model);
            } catch (Exception ignored) {

            }
        }
        return ResultUtil.success(models);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> start(int guestId, int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (!Objects.equals(guest.getType(), Constant.GuestType.USER)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "非用户主机由系统管理.");
        }

        this.checkSystemComponentComplete(guest.getNetworkId());
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
                guest.setHostId(hostId);
                guest.setStatus(Constant.GuestStatus.STARTING);
                this.guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
                BaseOperateParam operateParam = StartGuestOperate.builder().hostId(hostId).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("启动客户机[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            case Constant.GuestStatus.RUNNING:
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.CREATING:
            case Constant.GuestStatus.REBOOT:
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }


    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reboot(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {
            guest.setStatus(Constant.GuestStatus.REBOOT);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = RebootGuestOperate.builder().guestId(guestId)
                    .taskId(UUID.randomUUID().toString())
                    .title("重启客户机[" + guest.getDescription() + "]").build();
            this.operateTask.addTask(operateParam);
            this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> migrate(int guestId, int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {
            if (hostId == guest.getHostId()) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "迁移目标主机选择错误.");
            }
            BaseOperateParam operateParam = MigrateGuestOperate.builder()
                    .guestId(guestId)
                    .sourceHostId(guest.getHostId())
                    .toHostId(hostId)
                    .taskId(UUID.randomUUID().toString())
                    .title("迁移客户机[" + guest.getDescription() + "]").build();
            guest.setStatus(Constant.GuestStatus.MIGRATE);
            guest.setHostId(hostId);
            this.guestMapper.updateById(guest);
            this.operateTask.addTask(operateParam);
            this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> shutdown(int guestId, boolean force) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.RUNNING:
            case Constant.GuestStatus.STOPPING:
                guest.setStatus(Constant.GuestStatus.STOPPING);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = StopGuestOperate.builder().guestId(guestId).force(force)
                        .taskId(UUID.randomUUID().toString())
                        .title("关闭客户机[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            case Constant.GuestStatus.STOP:
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachCdRoom(int guestId, int templateId) {
        if (templateId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择光盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                guest.setCdRoom(templateId);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = ChangeGuestCdRoomOperate.builder().guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("挂载光驱[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachCdRoom(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                guest.setCdRoom(0);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = ChangeGuestCdRoomOperate.builder().guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("卸载光驱[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<AttachGuestVolumeModel> attachDisk(int guestId, int volumeId) {
        if (volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要挂载的磁盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                VolumeEntity volume = this.volumeMapper.selectById(volumeId);
                if (volume.getStatus() != Constant.VolumeStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未就绪.");
                }
                GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volume.getVolumeId()));
                if (guestDisk != null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
                }
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guestId));
                List<Integer> gustDiskDeviceIds = guestDiskList.stream().map(GuestDiskEntity::getDeviceId).collect(Collectors.toList());
                int deviceId = -1;
                for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
                    if (!gustDiskDeviceIds.contains(i)) {
                        deviceId = i;
                        break;
                    }
                }
                if (deviceId < 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前挂载超过最大磁盘数量限制");
                }
                guestDisk = GuestDiskEntity.builder().guestId(guestId).volumeId(volumeId).deviceId(deviceId).build();
                this.guestDiskMapper.insert(guestDisk);
                volume.setStatus(Constant.VolumeStatus.ATTACH_DISK);
                this.volumeMapper.updateById(volume);
                BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                        .deviceId(guestDisk.getDeviceId()).attach(true).volumeId(volumeId).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("挂载磁盘[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(AttachGuestVolumeModel.builder().guest(this.initGuestInfo(guest)).volume(this.initVolume(volume)).build());
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachDisk(int guestId, int guestDiskId) {
        if (guestDiskId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要卸载的磁盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);

        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestDiskEntity guestDisk = this.guestDiskMapper.selectById(guestDiskId);
                if (guestDisk == null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未挂载");
                }
                if (guestDisk.getGuestId() != guestId) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已挂载");
                }
                VolumeEntity volume = this.volumeMapper.selectById(guestDisk.getVolumeId());
                if (volume.getStatus() != Constant.VolumeStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未就绪.");
                }
                volume.setStatus(Constant.VolumeStatus.DETACH_DISK);
                this.volumeMapper.updateById(volume);
                this.guestDiskMapper.deleteById(guestDiskId);
                BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                        .deviceId(guestDisk.getDeviceId()).attach(false).volumeId(guestDisk.getVolumeId()).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("卸载磁盘[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                this.eventService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<AttachGuestNetworkModel> attachNetwork(int guestId, int networkId, String driveType) {
        if (networkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要挂载的网络");
        }
        if (StringUtils.isEmpty(driveType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要网络驱动");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
                List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guestId));
                List<Integer> guestNetworkDeviceIds = guestNetworkList.stream().map(GuestNetworkEntity::getDeviceId).collect(Collectors.toList());
                int deviceId = -1;
                for (int i = 0; i < Constant.MAX_DEVICE_ID; i++) {
                    if (!guestNetworkDeviceIds.contains(i)) {
                        deviceId = i;
                        break;
                    }
                }
                if (deviceId < 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前挂载超过最大网卡数量限制");
                }
                guestNetwork.setDeviceId(deviceId);
                guestNetwork.setDriveType(driveType);
                guestNetwork.setAllocateId(guestId);
                guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
                this.guestNetworkMapper.updateById(guestNetwork);
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(true).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("挂载网卡[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);

                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

                return ResultUtil.success(AttachGuestNetworkModel.builder().guest(this.initGuestInfo(guest)).network(this.initGuestNetwork(guestNetwork)).build());
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachNetwork(int guestId, int guestNetworkId) {
        if (guestNetworkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要卸载的网卡");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
            case Constant.GuestStatus.RUNNING:
                GuestNetworkEntity guestNetwork = this.guestNetworkMapper.selectById(guestNetworkId);
                if (guestNetwork == null || guestNetwork.getAllocateId() != guestId) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前网卡未挂载");
                }
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(false).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("卸载网卡[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyGuest(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return ResultUtil.<Void>builder().code(ErrorCode.GUEST_NOT_FOUND).build();
        }
        switch (guest.getStatus()) {
            case Constant.GuestStatus.ERROR: {
                guest.setStatus(Constant.GuestStatus.DESTROY);
                this.guestMapper.updateById(guest);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                DestroyGuestOperate operate = DestroyGuestOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁虚拟机[" + guest.getName() + "]").guestId(guest.getGuestId()).build();
                operateTask.addTask(operate);
                break;
            }
            case Constant.GuestStatus.STOP: {

                guest.setStatus(Constant.GuestStatus.DESTROY);
                this.guestMapper.updateById(guest);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                DestroyGuestOperate operate = DestroyGuestOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁虚拟机[" + guest.getName() + "]").guestId(guest.getGuestId()).build();
                operateTask.addTask(operate, guest.getType().equals(Constant.GuestType.USER) ? this.applicationConfig.getDestroyDelayMinute() : 0);
                break;
            }
            default:
                return ResultUtil.error(ErrorCode.SERVER_ERROR, "当前主机不是关机状态");
        }
        return ResultUtil.success();
    }

    public ResultUtil<String> getVncPassword(int guestId) {
        GuestVncEntity guestVnc = this.guestVncMapper.selectById(guestId);
        if (guestVnc == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机没有启动");
        }
        return ResultUtil.success(guestVnc.getPassword());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> modifyGuest(int guestId, int systemCategory, int bootstrapType, int groupId, String busType, String description, int schemeId) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的描述信息");
        }
        if (StringUtils.isEmpty(busType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择总线方式");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.CREATING:
            case Constant.GuestStatus.STOPPING:
            case Constant.GuestStatus.RUNNING:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不允许变更配置.");
            default:
                SchemeEntity scheme = this.schemeMapper.selectById(schemeId);
                guest.setDescription(description);
                guest.setBusType(busType);
                guest.setGroupId(groupId);
                guest.setSchemeId(scheme.getSchemeId());
                guest.setCpu(scheme.getCpu());
                guest.setMemory(scheme.getMemory());
                guest.setSpeed(scheme.getSpeed());
                guest.setSystemCategory(systemCategory);
                guest.setBootstrapType(bootstrapType);
                this.guestMapper.updateById(guest);
                this.eventService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
        }
    }

}
