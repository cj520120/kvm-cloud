package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
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
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.SymmetricCryptoUtil;
import cn.hutool.core.convert.impl.BeanConverter;
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

    private void initGuestMetaData(int guestId,String password){
        GuestEntity guest=this.guestMapper.selectById(guestId);
        Map<String, String> metaDataMap = new HashMap<>(4);
        String hostname="VM-" +guest.getGuestIp().replace(".", "-");
        metaDataMap.put("hostname", hostname);
        metaDataMap.put("local-hostname", hostname);
        metaDataMap.put("instance-id", guest.getName());
        this.metaMapper.delete(new QueryWrapper<MetaDataEntity>().eq("guest_id",guestId));
        for (Map.Entry<String, String> entry : metaDataMap.entrySet()) {
            MetaDataEntity metaDataEntity = MetaDataEntity.builder().guestId(guest.getGuestId()).metaKey(entry.getKey()).metaValue(entry.getValue()).build();
            this.metaMapper.insert(metaDataEntity);
        }
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
    }

    private boolean checkComponentComplete(int networkId, int componentType) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("component_type", componentType).eq("network_id", networkId).last("limit 0 ,1"));
        if (component == null) {
            return true;
        }
        GuestEntity vncGuest = this.guestMapper.selectById(component.getGuestId());
        return vncGuest == null || !Objects.equals(vncGuest.getStatus(), Constant.GuestStatus.RUNNING);
    }

    private void checkSystemComponentComplete(int networkId) {
        if (this.checkComponentComplete(networkId, Constant.ComponentType.SYSTEM)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "网络服务未初始化完成,请稍后重试");
        }
    }

    private GuestModel initGuestInfo(GuestEntity entity) {
        return new BeanConverter<>(GuestModel.class).convert(entity, null);
    }


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<>());
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());

        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listUserGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq("guest_type", Constant.GuestType.USER));
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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<List<GuestModel>> listSystemGuests(int networkId) {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq("network_id", networkId).eq("guest_type", Constant.GuestType.SYSTEM));
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机不存在");
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> createGuest(int groupId, String description, String busType
            , int hostId, int schemeId, int networkId, String networkDeviceType,
                                              int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                              int storageId, String volumeType, String password, long size) {
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
                .busType(busType)
                .cpu(scheme.getCpu())
                .speed(scheme.getSpeed())
                .memory(scheme.getMemory())
                .cdRoom(isoTemplateId)
                .hostId(0)
                .lastHostId(0)
                .schemeId(schemeId)
                .guestIp(guestNetwork.getIp())
                .networkId(networkId)
                .type(Constant.GuestType.USER)
                .status(Constant.GuestStatus.CREATING)
                .build();
        this.guestMapper.insert(guest);

        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(networkDeviceType);
        guestNetwork.setGuestId(guest.getGuestId());
        this.guestNetworkMapper.updateById(guestNetwork);
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
            createGuest(hostId, diskTemplateId, snapshotVolumeId, volumeType, password, size, uid, guest, storage);
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
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
        this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    private void createGuest(int hostId, int diskTemplateId, int snapshotVolumeId, String volumeType, String password, long size, String uid, GuestEntity guest, StorageEntity storage) {
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
        this.initGuestMetaData(guest.getGuestId(), password);
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .snapshotVolumeId(snapshotVolumeId)
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(hostId)
                .taskId(uid)
                .title("创建客户机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);

        this.clusterService.publish(NotifyData.builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());

        this.clusterService.publish(NotifyData.builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reInstall(int guestId, String password,int isoTemplateId, int diskTemplateId, int snapshotVolumeId, int volumeId,
                                            int storageId, String volumeType, long size) {

        if (isoTemplateId <= 0 && diskTemplateId <= 0 && snapshotVolumeId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() != Constant.GuestStatus.STOP) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "只能对关机状态对主机进行重装");
        }
        this.checkSystemComponentComplete(guest.getNetworkId());
        String uid = UUID.randomUUID().toString().replace("-", "");
        guest.setCdRoom(isoTemplateId);
        this.initGuestMetaData(guestId,password);
        this.guestDiskMapper.delete(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId).eq("device_id", 0));
        StorageEntity storage = this.allocateService.allocateStorage(storageId);
        if (volumeId <= 0) {
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
            guest.setStatus(Constant.GuestStatus.CREATING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .snapshotVolumeId(snapshotVolumeId)
                    .templateId(diskTemplateId)
                    .volumeId(volume.getVolumeId())
                    .taskId(uid)
                    .start(true)
                    .title("重装客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
            this.clusterService.publish(NotifyData.builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        } else {
            GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volumeId));
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
        this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> start(int guestId, int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        this.checkSystemComponentComplete(guest.getNetworkId());
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
                guest.setHostId(hostId);
                guest.setStatus(Constant.GuestStatus.STARTING);
                this.guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
                BaseOperateParam operateParam;
                if (Objects.equals(guest.getType(), Constant.GuestType.SYSTEM)) {
                    operateParam = StartComponentGuestOperate.builder().hostId(hostId).guestId(guestId)
                            .taskId(UUID.randomUUID().toString())
                            .title("启动系统主机[" + guest.getDescription() + "]").build();
                } else {
                    operateParam = StartGuestOperate.builder().hostId(hostId).guestId(guestId)
                            .taskId(UUID.randomUUID().toString())
                            .title("启动客户机[" + guest.getDescription() + "]").build();
                }
                this.operateTask.addTask(operateParam);
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
            this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
    }
    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> migrate (int guestId,int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {
            if(hostId==guest.getHostId()){
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
            this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            case Constant.GuestStatus.STOP:
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }

    }


    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态不正确.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volume.getVolumeId()));
                if (guestDisk != null) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘已经被挂载");
                }
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId));
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
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                this.clusterService.publish(NotifyData.builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(AttachGuestVolumeModel.builder().guest(this.initGuestInfo(guest)).volume(this.initVolume(volume)).build());
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前磁盘未挂载");
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
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                this.clusterService.publish(NotifyData.builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
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
                guestNetwork.setGuestId(guestId);
                this.guestNetworkMapper.updateById(guestNetwork);
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(true).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("挂载网卡[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);

                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

                return ResultUtil.success(AttachGuestNetworkModel.builder().guest(this.initGuestInfo(guest)).network(this.initGuestNetwork(guestNetwork)).build());
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }

    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
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
                if (guestNetwork == null || guestNetwork.getGuestId() != guestId) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前网卡未挂载");
                }
                BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                        .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(false).guestId(guestId)
                        .taskId(UUID.randomUUID().toString())
                        .title("卸载网卡[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机状态未就绪.");
        }
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<Void> destroyGuest(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return ResultUtil.<Void>builder().code(ErrorCode.GUEST_NOT_FOUND).build();
        }
        switch (guest.getStatus()) {
            case Constant.GuestStatus.ERROR:
            case Constant.GuestStatus.STOP:
                List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq("guest_id", guestId));
                for (GuestNetworkEntity guestNetwork : guestNetworkList) {
                    guestNetwork.setGuestId(0);
                    guestNetwork.setDeviceId(0);
                    guestNetwork.setDriveType("");
                    this.guestNetworkMapper.updateById(guestNetwork);
                }
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq("guest_id", guestId));
                for (GuestDiskEntity guestDisk : guestDiskList) {
                    this.guestDiskMapper.deleteById(guestDisk.getGuestDiskId());
                }
                if (!guestDiskList.isEmpty()) {
                    List<VolumeEntity> guestVolumeList = this.volumeMapper.selectBatchIds(guestDiskList.stream().map(GuestDiskEntity::getVolumeId).collect(Collectors.toList()));
                    for (VolumeEntity volume : guestVolumeList) {
                        volume.setStatus(Constant.VolumeStatus.DESTROY);
                        volumeMapper.updateById(volume);
                        DestroyVolumeOperate operate = DestroyVolumeOperate.builder().taskId(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).build();
                        operateTask.addTask(operate);
                        this.clusterService.publish(NotifyData.builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
                    }
                }
                this.guestVncMapper.deleteById(guestId);
                this.guestMapper.deleteById(guestId);
                this.guestPasswordMapper.deleteById(guestId);
                this.componentMapper.delete(new QueryWrapper<ComponentEntity>().eq("guest_id", guestId));
                this.metaMapper.delete(new QueryWrapper<MetaDataEntity>().eq("guest_id", guestId));
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

                this.clusterService.publish(NotifyData.builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());
                return ResultUtil.success();
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "当前主机不是关机状态");
        }
    }

    public ResultUtil<String> getVncPassword(int guestId) {
        GuestVncEntity guestVnc = this.guestVncMapper.selectById(guestId);
        if (guestVnc == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机没有启动");
        }
        return ResultUtil.success(guestVnc.getPassword());
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> modifyGuest(int guestId, int groupId, String busType, String description, int schemeId) {
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
                this.guestMapper.updateById(guest);
                this.clusterService.publish(NotifyData.builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
        }
    }

}
