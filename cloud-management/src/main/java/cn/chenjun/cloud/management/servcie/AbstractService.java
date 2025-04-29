package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.BeanConverter;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public abstract class AbstractService {
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    protected StorageMapper storageMapper;
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected HostMapper hostMapper;
    @Autowired
    protected VolumeMapper volumeMapper;
    @Autowired
    protected GuestDiskMapper guestDiskMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    @Lazy
    protected TaskService operateTask;
    @Autowired
    protected GuestVncMapper guestVncMapper;

    @Autowired
    protected SchemeMapper schemeMapper;
    @Autowired
    protected NotifyService notifyService;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected SshAuthorizedMapper sshAuthorizedMapper;
    @Autowired
    protected GuestSshMapper guestSshMapper;
    @Autowired
    protected ConfigService configService;
    @Autowired
    private GroupMapper groupMapper;

    protected boolean checkRouteComponentComplete(int networkId) {
        ComponentEntity component = this.componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.COMPONENT_TYPE, Constant.ComponentType.ROUTE).eq(ComponentEntity.NETWORK_ID, networkId).last("limit 0 ,1"));
        if (component == null) {
            return true;
        }
        List<Integer> componentGuestIds = GsonBuilderUtil.create().fromJson(component.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
        }.getType());
        componentGuestIds.add(component.getMasterGuestId());
        List<GuestEntity> componentGuestList = guestMapper.selectBatchIds(componentGuestIds).stream().filter(guestEntity -> Objects.equals(guestEntity.getStatus(), Constant.GuestStatus.RUNNING)).collect(Collectors.toList());
        return !componentGuestList.isEmpty();
    }

    protected GuestEntity getVolumeGuest(int volumeId) {
        GuestDiskEntity guestDisk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volumeId));
        if (guestDisk == null) {
            return null;
        }
        return guestMapper.selectById(guestDisk.getGuestId());
    }

    protected int getGuestMustStartHostId(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        return getGuestMustStartHostId(guest);
    }


    public int getGuestMustStartHostId(GuestEntity guest) {
        if (guest == null) {
            return 0;
        }
        int hostId = this.configService.getConfig(Collections.singletonList(ConfigQuery.builder().id(guest.getGuestId()).type(Constant.ConfigType.GUEST).build()), ConfigKey.VM_BIND_HOST);
        if (hostId == 0) {
            if (guest.getStatus().equals(Constant.GuestStatus.RUNNING) || guest.getStatus().equals(Constant.GuestStatus.STARTING) || guest.getStatus().equals(Constant.GuestStatus.STOPPING)) {
                hostId = guest.getHostId();
            } else {
                List<GuestDiskEntity> guestDiskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.GUEST_ID, guest.getGuestId()));
                List<Integer> volumeIds = guestDiskList.stream().map(GuestDiskEntity::getVolumeId).collect(Collectors.toList());
                List<VolumeEntity> guestVolumeList = this.volumeMapper.selectBatchIds(volumeIds);
                hostId = guestVolumeList.stream().map(VolumeEntity::getHostId).filter(id -> id > 0).findFirst().orElse(0);
            }
        }
        return hostId;
    }


    public GuestModel initGuestInfo(GuestEntity entity) {
        GuestModel model = BeanConverter.convert(entity, GuestModel.class);

        if (Objects.equals(entity.getType(), Constant.GuestType.COMPONENT)) {
            ComponentEntity component = componentMapper.selectById(entity.getOtherId());
            model.setComponent(BeanConverter.convert(component, ComponentGuestModel.class));
        }
        if (model.getGroupId() != 0) {
            model.setGroup(this.initGroup(this.groupMapper.selectById(model.getGroupId())));
        }
        if (model.getSchemeId() != 0) {
            model.setScheme(this.initScheme(schemeMapper.selectById(model.getSchemeId())));
        }
        if (model.getHostId() != 0) {
            model.setHost(this.initHost(hostMapper.selectById(model.getHostId())));
        }
        model.setNetwork(this.initNetwork(networkMapper.selectById(entity.getNetworkId())));
        if (model.getCdRoom() > 0) {
            model.setTemplate(this.initTemplateModel(templateMapper.selectById(entity.getCdRoom())));
        }
        return model;
    }

    protected StorageModel initStorageModel(StorageEntity entity) {
        StorageModel model = BeanConverter.convert(entity, StorageModel.class);
        if (model.getHostId() != 0) {
            model.setHost(this.initHost(hostMapper.selectById(model.getHostId())));
        }
        return model;
    }

    protected SchemeModel initScheme(SchemeEntity entity) {
        return BeanConverter.convert(entity, SchemeModel.class);
    }

    protected VolumeModel initVolume(GuestDiskEntity disk) {
        VolumeModel model = BeanConverter.convert(volumeMapper.selectById(disk.getVolumeId()), VolumeModel.class);
        model.setAttach(VolumeAttachModel.builder().guestId(disk.getGuestId()).deviceId(disk.getDeviceId()).deviceBus(disk.getDeviceBus()).guestDiskId(disk.getGuestDiskId()).build());
        return model;
    }


    protected VolumeModel initVolume(VolumeEntity volume) {
        GuestDiskEntity disk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq(GuestDiskEntity.VOLUME_ID, volume.getVolumeId()));
        return initVolume(volume, disk);
    }

    protected List<SimpleVolumeModel> initSimpleVolumeList(List<VolumeEntity> entityList) {
        List<SimpleVolumeModel> models = BeanConverter.convert(entityList, SimpleVolumeModel.class);
        List<Integer> volumeIds = models.stream().map(SimpleVolumeModel::getVolumeId).collect(Collectors.toList());
        List<GuestDiskEntity> diskList = this.guestDiskMapper.selectList(new QueryWrapper<GuestDiskEntity>().in(GuestDiskEntity.VOLUME_ID, volumeIds));

        List<Integer> guestIds = diskList.stream().map(GuestDiskEntity::getGuestId).collect(Collectors.toList());
        Map<Integer, GuestDiskEntity> diskMap = diskList.stream().collect(Collectors.toMap(GuestDiskEntity::getVolumeId, o -> o));
        Map<Integer, GuestEntity> guestMap = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().in(GuestEntity.GUEST_ID, guestIds)).stream().collect(Collectors.toMap(GuestEntity::getGuestId, o -> o));
        for (SimpleVolumeModel model : models) {
            GuestDiskEntity disk = diskMap.get(model.getVolumeId());
            if (disk != null) {
                model.setAttach(BeanConverter.convert(disk, VolumeAttachModel.class));
                GuestEntity guest = guestMap.get(disk.getGuestId());
                model.setGuest(BeanConverter.convert(guest, SimpleGuestModel.class));
            }
        }
        return models;

    }

    protected VolumeModel initVolume(VolumeEntity volume, GuestDiskEntity disk) {
        VolumeModel model = BeanConverter.convert(volume, VolumeModel.class);
        if (model.getHostId() != 0) {
            model.setHost(this.initHost(hostMapper.selectById(volume.getHostId())));
        }
        if (disk != null && disk.getGuestId() != 0) {
            model.setAttach(BeanConverter.convert(disk, VolumeAttachModel.class));
            model.setGuest(BeanConverter.convert(guestMapper.selectById(disk.getGuestId()), SimpleGuestModel.class));
        }
        model.setStorage(BeanConverter.convert(storageMapper.selectById(volume.getStorageId()), SimpleStorageModel.class));
        if (volume.getTemplateId() > 0) {
            model.setTemplate(BeanConverter.convert(templateMapper.selectById(volume.getTemplateId()), TemplateModel.class));
        }
        return model;
    }

    protected NetworkModel initNetwork(NetworkEntity entity) {
        if (entity == null) {
            return null;
        }
        NetworkModel model = BeanConverter.convert(entity, NetworkModel.class);
        if (model.getBasicNetworkId() != 0) {
            model.setBasic(initNetwork(networkMapper.selectById(model.getBasicNetworkId())));
        }
        return model;
    }

    protected SshAuthorizedModel initSshAuthorized(SshAuthorizedEntity entity) {
        return SshAuthorizedModel.builder().id(entity.getId()).name(entity.getSshName()).build();
    }

    protected GuestNetworkModel initNetwork(GuestNetworkEntity entity) {
        return GuestNetworkModel.builder().guestNetworkId(entity.getGuestNetworkId())
                .networkId(entity.getNetworkId())
                .ip(entity.getIp())
                .mac(entity.getMac())
                .guestId(entity.getAllocateId())
                .driveType(entity.getDriveType())
                .deviceId(entity.getDeviceId())
                .build();
    }

    protected HostModel initHost(HostEntity entity) {
        List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(entity.getHostId()).build());
        entity.setTotalCpu((int) (entity.getTotalCpu() * (Float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU)));
        entity.setTotalMemory((long) (entity.getTotalMemory() * (Float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY)));
        return BeanConverter.convert(entity, HostModel.class);
    }

    protected TemplateModel initTemplateModel(TemplateEntity entity) {
        return BeanConverter.convert(entity, TemplateModel.class);
    }

    protected ComponentDetailModel initComponent(ComponentEntity entity) {

        return ComponentDetailModel.builder().componentId(entity.getComponentId())
                .networkId(entity.getNetworkId())
                .componentSlaveNumber(entity.getComponentSlaveNumber())
                .componentType(entity.getComponentType())
                .masterGuestId(entity.getMasterGuestId())
                .componentVip(entity.getComponentVip())
                .basicComponentVip(entity.getBasicComponentVip())
                .slaveGuestIds(GsonBuilderUtil.create().fromJson(entity.getSlaveGuestIds(), new TypeToken<List<Integer>>() {
                }.getType())).build();
    }

    protected NatModel initNat(NatEntity entity) {
        return NatModel.builder().natId(entity.getNatId()).componentId(entity.getComponentId()).localPort(entity.getLocalPort()).protocol(entity.getProtocol()).remoteIp(entity.getRemoteIp()).remotePort(entity.getRemotePort()).build();
    }

    protected DnsModel initDns(DnsEntity entity) {
        if (entity == null) {
            return null;
        }
        return DnsModel.builder().id(entity.getDnsId())
                .networkId(entity.getNetworkId())
                .domain(entity.getDnsDomain())
                .ip(entity.getDnsIp())
                .build();
    }

    protected GroupModel initGroup(GroupInfoEntity entity) {
        if (entity == null) {
            return null;
        }
        return GroupModel.builder().groupId(entity.getGroupId()).groupName(entity.getGroupName()).createTime(entity.getCreateTime()).build();
    }
}
