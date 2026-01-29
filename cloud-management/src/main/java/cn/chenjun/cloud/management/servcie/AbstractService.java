package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
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
    protected NetworkMapper networkMapper;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    @Lazy
    protected TaskService operateTask;


    @Autowired
    protected SchemeMapper schemeMapper;
    @Autowired
    protected NotifyService notifyService;
    @Autowired
    protected ComponentMapper componentMapper;
    @Autowired
    protected SshAuthorizedMapper sshAuthorizedMapper;

    @Autowired
    protected ConfigService configService;
    @Autowired
    private GroupMapper groupMapper;


    protected String getVolumeType(StorageEntity storage) {
        String volumeType = this.configService.getConfig(ConfigKey.DEFAULT_DISK_TYPE);
        if (cn.chenjun.cloud.common.util.Constant.StorageType.CEPH_RBD.equals(storage.getType())) {
            volumeType = cn.chenjun.cloud.common.util.Constant.VolumeType.RAW;
        }
        return volumeType;
    }

    protected GuestEntity getVolumeGuest(int volumeId) {
        VolumeEntity volume = this.volumeMapper.selectById(volumeId);
        if (volume == null) {
            return null;
        }
        return this.guestMapper.selectById(volume.getGuestId());
    }

    protected int getGuestMustStartHostId(int guestId) {
        GuestEntity guest = this.guestMapper.selectById(guestId);
        return getGuestMustStartHostId(guest);
    }


    public int getGuestMustStartHostId(GuestEntity guest) {
        if (guest == null) {
            return 0;
        }
        return guest.getBindHostId();
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
        if(model.getBindHostId() != 0) {
            model.setBindHost(this.initHost(hostMapper.selectById(entity.getBindHostId())));
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


    protected VolumeModel initVolume(VolumeEntity volume) {
        VolumeModel model = BeanConverter.convert(volume, VolumeModel.class);
        if (model.getHostId() != 0) {
            model.setHost(this.initHost(hostMapper.selectById(volume.getHostId())));
        }
        if (volume.getGuestId() != 0) {
            model.setGuest(BeanConverter.convert(guestMapper.selectById(volume.getGuestId()), SimpleGuestModel.class));
        }
        model.setStorage(BeanConverter.convert(storageMapper.selectById(volume.getStorageId()), SimpleStorageModel.class));
        if (volume.getTemplateId() > 0) {
            model.setTemplate(BeanConverter.convert(templateMapper.selectById(volume.getTemplateId()), TemplateModel.class));
        }
        return model;
    }

    protected List<SimpleVolumeModel> initSimpleVolumeList(List<VolumeEntity> entityList) {
        List<SimpleVolumeModel> models = BeanConverter.convert(entityList, SimpleVolumeModel.class);
        List<Integer> guestIds = entityList.stream().map(VolumeEntity::getGuestId).collect(Collectors.toList());
        Map<Integer, GuestEntity> guestMap = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().in(GuestEntity.GUEST_ID, guestIds)).stream().collect(Collectors.toMap(GuestEntity::getGuestId, o -> o));
        for (SimpleVolumeModel model : models) {
            GuestEntity guest = guestMap.get(model.getGuestId());
            if (guest != null) {
                model.setGuest(BeanConverter.convert(guest, SimpleGuestModel.class));
            }
        }
        return models;

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
                .componentType(entity.getComponentType())
                .componentVip(entity.getComponentVip())
                .basicComponentVip(entity.getBasicComponentVip()) .build();
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
