package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.operate.bean.*;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.*;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
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
public class GuestService extends AbstractService {
    @Autowired
    private AllocateService allocateService;

    private void checkSystemComponentComplete(int networkId) {
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.NETWORK).id(networkId).build());
        if (Objects.equals(this.configService.getConfig(queryList, ConfigKey.SYSTEM_COMPONENT_ENABLE), Constant.Enable.NO)) {
            return;
        }
        if (!this.checkRouteComponentComplete(networkId)) {
            throw new CodeException(ErrorCode.NETWORK_NOT_READY, "网络服务未初始化完成,请稍后重试");
        }
    }


    public ResultUtil<List<SimpleGuestModel>> listGuests() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<>());
        List<SimpleGuestModel> models = BeanConverter.convert(guestList, SimpleGuestModel.class);
        return ResultUtil.success(models);
    }

    public ResultUtil<Page<SimpleGuestModel>> search(Integer guestType, Integer groupId, Integer networkId, Integer hostId, Integer schemeId, Integer status, String keyword, int no, int size) {
        QueryWrapper queryWrapper = new QueryWrapper<GuestEntity>();
        queryWrapper.eq(groupId != null, GuestEntity.GROUP_ID, groupId);
        queryWrapper.eq(hostId != null, GuestEntity.HOST_ID, hostId);
        queryWrapper.eq(networkId != null, GuestEntity.NETWORK_ID, networkId);
        queryWrapper.eq(schemeId != null, GuestEntity.SCHEME_ID, schemeId);
        queryWrapper.eq(guestType != null, GuestEntity.GUEST_TYPE, guestType);
        queryWrapper.eq(status != null, GuestEntity.GUEST_STATUS, status);
        if (!ObjectUtils.isEmpty(keyword)) {
            queryWrapper.and(o -> {
                String condition = "%" + keyword + "%";
                QueryWrapper<GuestEntity> wrapper = (QueryWrapper) o;
                wrapper.like(GuestEntity.GUEST_IP, condition)
                        .or().like(GuestEntity.GUEST_NAME, condition)
                        .or().like(GuestEntity.GUEST_DESCRIPTION, condition);
            });
        }
        int nCount = Math.toIntExact(this.guestMapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<GuestEntity> guestList = this.guestMapper.selectList(queryWrapper);
        List<SimpleGuestModel> models = BeanConverter.convert(guestList, SimpleGuestModel.class);
        Page<SimpleGuestModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
    }


    public ResultUtil<List<GuestModel>> listSystemGuests(int componentId) {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq(GuestEntity.OTHER_ID, componentId).eq(GuestEntity.GUEST_TYPE, Constant.GuestType.COMPONENT));
        List<GuestModel> models = guestList.stream().map(this::initGuestInfo).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    public ResultUtil<GuestModel> getGuestInfo(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return ResultUtil.error(ErrorCode.GUEST_NOT_FOUND, "虚拟机不存在");
        }
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> createGuest(int groupId, String description, int systemCategory, int bootstrapType, String deviceDriver,
                                              int hostId, int schemeId, int networkId, String networkDeviceType,
                                              int isoTemplateId, int diskTemplateId, int volumeId,
                                              int storageId, long size, String hostName, String password, int sshId) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入有效的描述信息");
        }
        if (StringUtils.isEmpty(deviceDriver)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择磁盘驱动");
        }
        if (StringUtils.isEmpty(networkDeviceType)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择网卡驱动");
        }
        if (isoTemplateId <= 0 && diskTemplateId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        if (isoTemplateId > 0 && size <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入磁盘大小");
        }
        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME, storageId);
        if (Objects.equals(storage.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
            if (hostId > 0 && !Objects.equals(hostId, storage.getHostId())) {
                throw new CodeException(ErrorCode.PARAM_ERROR, "使用本地存储时，只能在存储所在机器启动");
            }
            hostId = storage.getHostId();
        }
        this.checkSystemComponentComplete(networkId);
        SchemeEntity scheme = this.schemeMapper.selectById(schemeId);
        GuestNetworkEntity guestNetwork = this.allocateService.allocateNetwork(networkId);
        GuestEntity guest = GuestEntity.builder()
                .groupId(groupId)
                .name(NameUtil.generateGuestName())
                .description(description)
                .systemCategory(systemCategory)
                .bootstrapType(bootstrapType)
                .cpu(scheme.getCpu())
                .share(scheme.getShare())
                .memory(scheme.getMemory())
                .cdRoom(isoTemplateId)
                .hostId(0)
                .lastHostId(0)
                .schemeId(schemeId)
                .guestIp(guestNetwork.getIp())
                .otherId(0)
                .networkId(networkId)
                .type(Constant.GuestType.USER)
                .extern("{}")
                .status(Constant.GuestStatus.CREATING)
                .createTime(new Date())
                .build();
        this.guestMapper.insert(guest);
        SshAuthorizedEntity ssh = sshId <= 0 ? null : this.sshAuthorizedMapper.selectById(sshId);
        Map<String, Map<String, String>> externData = new HashMap<>();
        externData.put(GuestExternNames.META_DATA, GuestExternUtil.buildMetaDataParam(guest, hostName));
        externData.put(GuestExternNames.USER_DATA, GuestExternUtil.buildUserDataParam(guest, password, Optional.ofNullable(ssh).map(SshAuthorizedEntity::getSshPublicKey).orElse("")));
        externData.put(GuestExternNames.VNC, GuestExternUtil.buildVncParam(guest, "", "5900"));
        guest.setExtern(GsonBuilderUtil.create().toJson(externData));
        this.guestMapper.updateById(guest);
        guestNetwork.setDeviceId(0);
        guestNetwork.setDriveType(networkDeviceType);
        guestNetwork.setAllocateId(guest.getGuestId());
        guestNetwork.setAllocateType(Constant.NetworkAllocateType.GUEST);
        this.guestNetworkMapper.updateById(guestNetwork);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        if (volumeId <= 0) {
            createGuest(hostId, diskTemplateId, deviceDriver, volumeType, size, guest, storage);
        } else {
            VolumeEntity volume = this.volumeMapper.selectById(volumeId);
            if (volume == null) {
                throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
            }
            if (volume.getGuestId() > 0) {
                throw new CodeException(ErrorCode.GUEST_VOLUME_HAS_ATTACH_ERROR, "磁盘已分配给其他客户机");
            }
            volume.setGuestId(guest.getGuestId());
            volume.setDeviceId(0);
            volume.setDeviceDriver(deviceDriver);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(hostId)
                    .id(UUID.randomUUID().toString())
                    .title("启动客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    private void createGuest(int hostId, int diskTemplateId, String deviceType, String volumeType, long size, GuestEntity guest, StorageEntity storage) {
        VolumeEntity volume = createGuestVolume(diskTemplateId, deviceType, volumeType, size, guest, storage);
        BaseOperateParam operateParam = CreateGuestOperate.builder()
                .guestId(guest.getGuestId())
                .templateId(diskTemplateId)
                .volumeId(volume.getVolumeId())
                .start(true)
                .hostId(hostId)
                .id(UUID.randomUUID().toString())
                .title("创建客户机[" + guest.getDescription() + "]")
                .build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());
    }

    private VolumeEntity createGuestVolume(int diskTemplateId, String deviceDriver, String volumeType, long size, GuestEntity guest, StorageEntity storage) {
        String volumeName = NameUtil.generateVolumeName();
        VolumeEntity volume = VolumeEntity.builder()
                .description("ROOT-" + guest.getGuestId())
                .capacity(size)
                .storageId(storage.getStorageId())
                .hostId(storage.getHostId())
                .name(volumeName)
                .path(storage.getMountPath() + "/" + volumeName)
                .type(volumeType)
                .templateId(diskTemplateId)
                .allocation(0L)
                .capacity(size)
                .guestId(0)
                .guestId(guest.getGuestId())
                .deviceDriver(deviceDriver)
                .status(Constant.VolumeStatus.CREATING)
                .build();
        this.volumeMapper.insert(volume);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return volume;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reInstall(int guestId, String deviceDriver, int systemCategory, int bootstrapType, int isoTemplateId, int diskTemplateId, int volumeId,
                                            int storageId, long size) {

        if (isoTemplateId <= 0 && diskTemplateId <= 0 && volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择系统来源");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() != Constant.GuestStatus.STOP) {
            throw new CodeException(ErrorCode.GUEST_NOT_STOP, "只能对关机状态的主机进行重装操作");
        }
        this.checkSystemComponentComplete(guest.getNetworkId());
        guest.setCdRoom(isoTemplateId);
        guest.setSystemCategory(systemCategory);
        guest.setBootstrapType(bootstrapType);

        VolumeEntity volume = this.volumeMapper.selectOne(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guestId).eq(VolumeEntity.DEVICE_ID, 0));
        volume.setGuestId(0);
        this.volumeMapper.updateById(volume);
        StorageEntity storage = this.allocateService.allocateStorage(Constant.StorageSupportCategory.VOLUME, storageId);
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        if (volumeId <= 0) {
            volume = createGuestVolume(diskTemplateId, deviceDriver, volumeType, size, guest, storage);
            guest.setStatus(Constant.GuestStatus.CREATING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = CreateGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .templateId(diskTemplateId)
                    .volumeId(volume.getVolumeId())
                    .id(UUID.randomUUID().toString())
                    .hostId(0)
                    .start(true)
                    .title("重装客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        } else {
            volume = this.volumeMapper.selectById(volumeId);
            if (volume == null) {
                throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
            }
            if (volume.getGuestId() > 0) {
                throw new CodeException(ErrorCode.GUEST_VOLUME_HAS_ATTACH_ERROR, "磁盘已分配给其他客户机");
            }
            volume.setGuestId(guest.getGuestId());
            volume.setDeviceDriver(deviceDriver);
            volume.setDeviceId(0);
            this.volumeMapper.updateById(volume);
            guest.setStatus(Constant.GuestStatus.STARTING);
            this.guestMapper.updateById(guest);
            BaseOperateParam operateParam = StartGuestOperate.builder()
                    .guestId(guest.getGuestId())
                    .hostId(volume.getHostId())
                    .id(UUID.randomUUID().toString())
                    .title("重装客户机[" + guest.getDescription() + "]")
                    .build();
            this.operateTask.addTask(operateParam);
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
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
            throw new CodeException(ErrorCode.GUEST_NOT_ALLOW_USER_OPERATION, "非用户主机由系统管理.");
        }
        this.checkSystemComponentComplete(guest.getNetworkId());
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
                int allowHostId = getGuestMustStartHostId(guest);
                if (hostId == 0) {
                    hostId = allowHostId;
                } else if (hostId != allowHostId && allowHostId > 0) {
                    throw new CodeException(ErrorCode.GUEST_BIND_OTHER_HOST, "该宿主机已经绑定了启动主机ID：" + allowHostId);
                }
                guest.setHostId(0);
                guest.setStatus(Constant.GuestStatus.STARTING);
                this.guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
                BaseOperateParam operateParam = StartGuestOperate.builder().hostId(hostId).guestId(guestId)
                        .id(UUID.randomUUID().toString())
                        .title("启动客户机[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            case Constant.GuestStatus.RUNNING:
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.CREATING:
            case Constant.GuestStatus.REBOOT:
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.GUEST_NOT_STOP, "当前主机状态不正确.");
        }


    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> reboot(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.RUNNING:
                guest.setStatus(Constant.GuestStatus.REBOOT);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = RebootGuestOperate.builder().guestId(guestId)
                        .id(UUID.randomUUID().toString())
                        .title("重启客户机[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.GUEST_NOT_START, "当前主机状态不正确.");

        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> migrate(int guestId, int hostId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {
            if (hostId == guest.getHostId()) {
                throw new CodeException(ErrorCode.BASE_HOST_ERROR, "迁移目标主机选择错误.");
            }
            BaseOperateParam operateParam = MigrateGuestOperate.builder()
                    .guestId(guestId)
                    .sourceHostId(guest.getHostId())
                    .toHostId(hostId)
                    .id(UUID.randomUUID().toString())
                    .title("迁移客户机[" + guest.getDescription() + "]").build();
            guest.setStatus(Constant.GuestStatus.MIGRATE);
            guest.setHostId(hostId);
            this.guestMapper.updateById(guest);
            this.operateTask.addTask(operateParam);
            this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
            return ResultUtil.success(this.initGuestInfo(guest));
        }
        throw new CodeException(ErrorCode.GUEST_NOT_START, "当前主机状态不正确.");
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> shutdown(int guestId, boolean force) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.RUNNING:
            case Constant.GuestStatus.REBOOT:
            case Constant.GuestStatus.STOPPING:
                guest.setStatus(Constant.GuestStatus.STOPPING);
                this.guestMapper.updateById(guest);
                BaseOperateParam operateParam = StopGuestOperate.builder().guestId(guestId).force(force)
                        .id(UUID.randomUUID().toString())
                        .title("关闭客户机[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
            case Constant.GuestStatus.STOP:
                return ResultUtil.success(this.initGuestInfo(guest));
            default:
                throw new CodeException(ErrorCode.GUEST_NOT_START, "当前主机状态不正确.");
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> attachCdRoom(int guestId, int templateId) {
        if (templateId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择光盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        guest.setCdRoom(templateId);

        this.guestMapper.updateById(guest);
        BaseOperateParam operateParam = ChangeGuestCdRoomOperate.builder().guestId(guestId).cdRoom(templateId)
                        .id(UUID.randomUUID().toString())
                        .title("挂载光驱[" + guest.getDescription() + "]").build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<VolumeModel> modifyGuestDiskDeviceType(int guestId, int deviceId, String deviceDriver) {
        VolumeEntity volume = this.volumeMapper.selectOne(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guestId).eq(VolumeEntity.DEVICE_ID, deviceId));
        if (volume == null) {
            throw new CodeException(ErrorCode.GUEST_VOLUME_NOT_ATTACH_ERROR, "选择的磁盘没有挂载");
        }
        volume.setDeviceDriver(deviceDriver);
        this.volumeMapper.updateById(volume);
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initVolume(volume));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachCdRoom(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
                guest.setCdRoom(0);
                this.guestMapper.updateById(guest);
        BaseOperateParam operateParam = ChangeGuestCdRoomOperate.builder().guestId(guestId).cdRoom(0)
                        .id(UUID.randomUUID().toString())
                        .title("卸载光驱[" + guest.getDescription() + "]").build();
                this.operateTask.addTask(operateParam);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<AttachGuestVolumeModel> attachDisk(int guestId, int volumeId, String deviceDriver) {
        if (volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要挂载的磁盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume.getHostId() > 0) {
            int allowHostId = getGuestMustStartHostId(guest);
            if (allowHostId > 0 && allowHostId != volume.getHostId()) {
                throw new CodeException(ErrorCode.GUEST_BIND_OTHER_HOST, "当前主机无法挂载其他主机的本地磁盘");
            }
        }
        if (volume.getStatus() != Constant.VolumeStatus.READY) {
            throw new CodeException(ErrorCode.VOLUME_NOT_READY, "当前磁盘未就绪.");
        }


        if (volume.getGuestId() > 0) {
            throw new CodeException(ErrorCode.GUEST_VOLUME_HAS_ATTACH_ERROR, "当前磁盘已经被挂载");
        }
        List<VolumeEntity> volumes = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guestId));

        List<Integer> gustDiskDeviceIds = volumes.stream().map(VolumeEntity::getDeviceId).collect(Collectors.toList());
        int deviceId = 0;
        do {
            deviceId++;
        } while (gustDiskDeviceIds.contains(deviceId));
        volume.setDeviceId(deviceId);
        volume.setGuestId(guestId);
        volume.setDeviceDriver(deviceDriver);
        this.volumeMapper.updateById(volume);
        BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                .deviceId(volume.getDeviceId()).deviceBus(volume.getDeviceDriver()).attach(true).volumeId(volumeId).guestId(guestId)
                        .id(UUID.randomUUID().toString())
                        .title("挂载磁盘[" + guest.getDescription() + "]").build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(AttachGuestVolumeModel.builder().guest(this.initGuestInfo(guest)).volume(this.initVolume(volume)).build());

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachDisk(int guestId, int volumeId) {
        if (volumeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要卸载的磁盘");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在");
        }
        if (volume.getGuestId() != guestId) {
            throw new CodeException(ErrorCode.GUEST_VOLUME_NOT_ATTACH_ERROR, "当前磁盘未挂载");
        }
        volume.setGuestId(0);
        this.volumeMapper.updateById(volume);
        BaseOperateParam operateParam = ChangeGuestDiskOperate.builder()
                .deviceId(volume.getDeviceId()).deviceBus(volume.getDeviceDriver()).attach(false).volumeId(volume.getVolumeId()).guestId(guestId)
                        .id(UUID.randomUUID().toString())
                        .title("卸载磁盘[" + guest.getDescription() + "]").build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
        return ResultUtil.success(this.initGuestInfo(guest));

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
                        .id(UUID.randomUUID().toString())
                        .title("挂载网卡[" + guest.getDescription() + "]").build();
        this.operateTask.addTask(operateParam);

        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

        return ResultUtil.success(AttachGuestNetworkModel.builder().guest(this.initGuestInfo(guest)).network(this.initNetwork(guestNetwork)).build());


    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> detachNetwork(int guestId, int guestNetworkId) {
        if (guestNetworkId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择需要卸载的网卡");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        GuestNetworkEntity guestNetwork = this.guestNetworkMapper.selectById(guestNetworkId);
        if (guestNetwork == null || guestNetwork.getAllocateId() != guestId) {
            throw new CodeException(ErrorCode.NETWORK_NIC_NOT_ATTACH, "当前网卡未挂载");
        }
        BaseOperateParam operateParam = ChangeGuestNetworkInterfaceOperate.builder()
                .guestNetworkId(guestNetwork.getGuestNetworkId()).attach(false).guestId(guestId)
                .id(UUID.randomUUID().toString())
                .title("卸载网卡[" + guest.getDescription() + "]").build();
        this.operateTask.addTask(operateParam);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        return ResultUtil.success(this.initGuestInfo(guest));

    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> destroyGuest(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            return ResultUtil.<GuestModel>builder().code(ErrorCode.GUEST_NOT_FOUND).build();
        }
        int timeout = 0;
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STOP:
                if (guest.getType().equals(Constant.GuestType.USER)) {
                    timeout = configService.getConfig(ConfigKey.DEFAULT_DESTROY_DELAY_MINUTE);
                }
                break;
            case Constant.GuestStatus.ERROR:
            case Constant.GuestStatus.DESTROY:
                break;
            default:
                return ResultUtil.error(ErrorCode.GUEST_NOT_STOP, "当前主机不是关机状态");

        }
        guest.setStatus(Constant.GuestStatus.DESTROY);
        this.guestMapper.updateById(guest);
        this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
        DestroyGuestOperate operate = DestroyGuestOperate.builder().id(UUID.randomUUID().toString()).title("销毁虚拟机[" + guest.getName() + "]").guestId(guest.getGuestId()).build();
        operateTask.addTask(operate, timeout);
        return ResultUtil.success(this.initGuestInfo(guest));
    }

    public ResultUtil<String> getVncPassword(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        if (guest == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机不存在");
        }
        Map<String, Map<String, String>> externMap = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
        }.getType());
        Map<String, String> vncMap = externMap.computeIfAbsent(GuestExternNames.VNC, k -> GuestExternUtil.buildVncParam(guest, "", "5900"));
        return ResultUtil.success(vncMap.get(GuestExternNames.VncNames.PASSWORD));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultUtil<GuestModel> modifyGuest(int guestId, int systemCategory, int bootstrapType, int groupId, String description, int schemeId) {
        if (StringUtils.isEmpty(description)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请输入合法的描述信息");
        }
        if (schemeId <= 0) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "请选择架构方案");
        }
        GuestEntity guest = this.guestMapper.selectById(guestId);
        switch (guest.getStatus()) {
            case Constant.GuestStatus.STARTING:
            case Constant.GuestStatus.RUNNING:
                throw new CodeException(ErrorCode.GUEST_IS_RUNNING_ERROR, "当前主机状态不允许变更配置.");
            default:
                SchemeEntity scheme = this.schemeMapper.selectById(schemeId);
                guest.setDescription(description);
                guest.setGroupId(groupId);
                guest.setSchemeId(scheme.getSchemeId());
                guest.setCpu(scheme.getCpu());
                guest.setMemory(scheme.getMemory());
                guest.setShare(scheme.getShare());
                guest.setSystemCategory(systemCategory);
                guest.setBootstrapType(bootstrapType);
                this.guestMapper.updateById(guest);
                this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
                return ResultUtil.success(this.initGuestInfo(guest));
        }
    }

}
