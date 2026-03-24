package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.RequestContextHolderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConvertService {

    @Autowired
    private LockRunner lockRunner;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private SchemeService schemeService;
    @Autowired
    private HostService hostService;
    @Autowired
    private NetworkService networkService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private GuestService guestService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ConfigService configService;


    public List<GuestModel> initGuestList(List<GuestEntity> entityList) {
        List<Integer> hostIds = entityList.stream().map(GuestEntity::getHostId).collect(Collectors.toList());
        hostIds.addAll(entityList.stream().map(GuestEntity::getBindHostId).distinct().collect(Collectors.toList()));
        List<Integer> groupIds = entityList.stream().map(GuestEntity::getGroupId).distinct().collect(Collectors.toList());
        List<Integer> schemeIds = entityList.stream().map(GuestEntity::getSchemeId).distinct().collect(Collectors.toList());
        List<Integer> networkIds = entityList.stream().map(GuestEntity::getNetworkId).distinct().collect(Collectors.toList());
        List<Integer> templateIds = entityList.stream().map(GuestEntity::getCdRoom).distinct().collect(Collectors.toList());
        List<Integer> componentIds = entityList.stream().filter(guest -> Objects.equals(guest.getType(), Constant.GuestType.COMPONENT)).map(GuestEntity::getOtherId).distinct().collect(Collectors.toList());
        Map<Integer, HostEntity> hostMap = this.hostService.listHostByIds(hostIds).stream().collect(Collectors.toMap(HostEntity::getHostId, h -> h));
        Map<Integer, GroupEntity> groupMap = this.groupService.listGroupByIds(groupIds).stream().collect(Collectors.toMap(GroupEntity::getGroupId, g -> g));
        Map<Integer, SchemeEntity> schemeMap = this.schemeService.listSchemeByIds(schemeIds).stream().collect(Collectors.toMap(SchemeEntity::getSchemeId, s -> s));
        Map<Integer, NetworkEntity> networkMap = this.networkService.listNetworkByIds(networkIds).stream().collect(Collectors.toMap(NetworkEntity::getNetworkId, n -> n));
        Map<Integer, TemplateEntity> templateMap = this.templateService.listTemplateByIds(templateIds).stream().collect(Collectors.toMap(TemplateEntity::getTemplateId, t -> t));
        Map<Integer, ComponentEntity> componentMap = this.componentService.listComponentByIds(componentIds).stream().collect(Collectors.toMap(ComponentEntity::getComponentId, c -> c));
        List<GuestModel> models = new ArrayList<>();
        for (GuestEntity entity : entityList) {
            GuestModel model = BeanConverter.convert(entity, GuestModel.class);
            if (Objects.equals(entity.getType(), Constant.GuestType.COMPONENT)) {
                ComponentEntity component = componentMap.get(entity.getOtherId());
                model.setComponent(BeanConverter.convert(component, ComponentGuestModel.class));
            }
            if (model.getGroupId() != 0) {
                model.setGroup(this.initGroupModel(groupMap.get(model.getGroupId())));
            }
            if (model.getSchemeId() != 0) {
                model.setScheme(this.initSchemeModel(schemeMap.get(model.getSchemeId())));
            }
            if (model.getHostId() != 0) {
                model.setHost(this.initHostModel(hostMap.get(model.getHostId())));
            }
            if (model.getBindHostId() != 0) {
                model.setBindHost(this.initHostModel(hostMap.get((entity.getBindHostId()))));
            }
            model.setNetwork(this.initNetworkModel(networkMap.get(entity.getNetworkId())));
            if (model.getCdRoom() > 0) {
                model.setTemplate(this.initTemplateModel(templateMap.get(entity.getCdRoom())));
            }
            models.add(model);
        }
        return models;
    }

    public GuestModel initGuestModel(GuestEntity entity) {
        if (entity == null) {
            return null;
        }
        return initGuestList(Collections.singletonList(entity)).get(0);
    }

    public StorageModel initStorageModel(StorageEntity entity) {
        StorageModel model = BeanConverter.convert(entity, StorageModel.class);
        if (model.getHostId() != 0) {
            model.setHost(this.initHostModel(FunctionUtils.ignoreErrorCall(() -> this.hostService.getHostById(model.getHostId()))));
        }
        return model;
    }

    public SchemeModel initSchemeModel(SchemeEntity entity) {
        return BeanConverter.convert(entity, SchemeModel.class);
    }


    public VolumeModel initVolumeModel(VolumeEntity volume) {
        VolumeModel model = BeanConverter.convert(volume, VolumeModel.class);
        if (model.getHostId() != 0) {
            model.setHost(this.initHostModel(RequestContextHolderUtil.get("Host." + volume.getHostId(), () -> this.hostService.getHostById(volume.getHostId()))));
        }
        if (volume.getGuestId() != 0) {
            model.setGuest(BeanConverter.convert(RequestContextHolderUtil.get("Guest." + volume.getGuestId(), () ->this.guestService.getGuestById(volume.getGuestId())), SimpleGuestModel.class));
        }
        model.setStorage(BeanConverter.convert(RequestContextHolderUtil.get("Storage." + volume.getStorageId(), () -> this.storageService.getStorageById(volume.getStorageId())), SimpleStorageModel.class));
        if (volume.getTemplateId() > 0) {
            model.setTemplate(BeanConverter.convert(RequestContextHolderUtil.get("Template." + volume.getTemplateId(), () -> this.templateService.getTemplateById(volume.getTemplateId())), TemplateModel.class));
        }
        return model;
    }

    public List<SimpleVolumeModel> initSimpleVolumeModels(List<VolumeEntity> entityList) {
        List<SimpleVolumeModel> models = BeanConverter.convert(entityList, SimpleVolumeModel.class);
        List<Integer> guestIds = entityList.stream().map(VolumeEntity::getGuestId).collect(Collectors.toList());
        Map<Integer, GuestEntity> guestMap = this.guestService.selectByIds(guestIds).stream().collect(Collectors.toMap(GuestEntity::getGuestId, o -> o));
        for (SimpleVolumeModel model : models) {
            GuestEntity guest = guestMap.get(model.getGuestId());
            if (guest != null) {
                model.setGuest(BeanConverter.convert(guest, SimpleGuestModel.class));
            }
        }
        return models;

    }

    public NetworkModel initNetworkModel(NetworkEntity entity) {
        if (entity == null) {
            return null;
        }
        NetworkModel model = BeanConverter.convert(entity, NetworkModel.class);
        if (model.getBasicNetworkId() != 0) {
            model.setBasic(initNetworkModel(RequestContextHolderUtil.get("Network." + model.getBasicNetworkId(), () -> this.networkService.getNetworkById(model.getBasicNetworkId()))));
        }
        return model;
    }

    public SshAuthorizedModel initSshModel(SshAuthorizedEntity entity) {
        return SshAuthorizedModel.builder().id(entity.getId()).name(entity.getSshName()).build();
    }

    public GuestNetworkModel initGuestNetworkModel(GuestNetworkEntity entity) {
        return GuestNetworkModel.builder().guestNetworkId(entity.getGuestNetworkId())
                .networkId(entity.getNetworkId())
                .ip(entity.getIp())
                .mac(entity.getMac())
                .guestId(entity.getAllocateId())
                .driveType(entity.getDeviceType())
                .deviceId(entity.getDeviceId())
                .build();
    }

    public HostModel initHostModel(HostEntity entity) {
        List<ConfigQuery> queryList = Arrays.asList(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).build(), ConfigQuery.builder().type(Constant.ConfigType.HOST).id(entity.getHostId()).build());


        float overCpu = RequestContextHolderUtil.get("Over.Cpu", () -> (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_CPU));
        float overMemory = RequestContextHolderUtil.get("Over.Memory", () -> (float) this.configService.getConfig(queryList, ConfigKey.DEFAULT_OVER_MEMORY));

        entity.setTotalCpu((int) (entity.getTotalCpu() * overCpu));
        entity.setTotalMemory((long) (entity.getTotalMemory() * overMemory));
        return BeanConverter.convert(entity, HostModel.class);
    }

    public TemplateModel initTemplateModel(TemplateEntity entity) {
        return BeanConverter.convert(entity, TemplateModel.class);
    }

    public ComponentDetailModel initComponentModel(ComponentEntity entity) {

        return ComponentDetailModel.builder().componentId(entity.getComponentId())
                .networkId(entity.getNetworkId())
                .componentType(entity.getComponentType())
                .componentVip(entity.getComponentVip())
                .basicComponentVip(entity.getBasicComponentVip())
                .createTime(entity.getCreateTime()).build();
    }

    public NatModel initNatModel(NatEntity entity) {
        return NatModel.builder().natId(entity.getNatId()).componentId(entity.getComponentId()).localPort(entity.getLocalPort()).protocol(entity.getProtocol()).remoteIp(entity.getRemoteIp()).remotePort(entity.getRemotePort()).build();
    }

    public DnsModel initDnsModel(DnsEntity entity) {
        if (entity == null) {
            return null;
        }
        return DnsModel.builder().id(entity.getDnsId())
                .networkId(entity.getNetworkId())
                .domain(entity.getDnsDomain())
                .ip(entity.getDnsIp())
                .build();
    }

    public GroupModel initGroupModel(GroupEntity entity) {
        if (entity == null) {
            return null;
        }
        return GroupModel.builder().groupId(entity.getGroupId()).groupName(entity.getGroupName()).createTime(entity.getCreateTime()).build();
    }

    public NicMode initNicModel(GuestNetworkEntity entity) {
        return NicMode.builder().id(entity.getGuestNetworkId()).allocateType(entity.getAllocateType()).allocateId(entity.getAllocateId()).allocateDescription(entity.getAllocateDescription()).mac(entity.getMac()).ip(entity.getIp()).build();
    }

    public UserModel initUserModel(UserEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        userModel.setUserId(loginInfoEntity.getUserId());
        userModel.setUserName(loginInfoEntity.getUserName());
        userModel.setLoginType(loginInfoEntity.getLoginType());
        userModel.setUserType(loginInfoEntity.getUserType());
        userModel.setLoginName(loginInfoEntity.getLoginName());
        userModel.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        userModel.setUserStatus(loginInfoEntity.getUserStatus());
        userModel.setRegisterTime(loginInfoEntity.getCreateTime());
        return userModel;
    }
}
