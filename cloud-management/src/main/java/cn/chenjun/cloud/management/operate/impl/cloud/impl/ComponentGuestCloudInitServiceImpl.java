package cn.chenjun.cloud.management.operate.impl.cloud.impl;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.dao.ComponentDao;
import cn.chenjun.cloud.management.data.dao.ComponentGuestDao;
import cn.chenjun.cloud.management.data.dao.NetworkDao;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.impl.cloud.CloudInitService;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudData;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.ComponentInitialization;
import cn.chenjun.cloud.management.util.CloudInitIsoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComponentGuestCloudInitServiceImpl implements CloudInitService {
    @Autowired
    private ComponentGuestDao componentGuestDao;
    @Autowired
    private List<ComponentInitialization> componentInitializations;
    @Autowired
    private ComponentDao componentDao;
    @Autowired
    private NetworkDao networkDao;

    @Override
    public CloudData build(GuestEntity guest, HostEntity host) {
        ComponentGuestEntity componentGuest = componentGuestDao.findByGuestId(guest.getGuestId());
        if (componentGuest == null) {
            return null;
        }
        ComponentEntity component = componentDao.findById(componentGuest.getComponentId());
        if (component == null) {
            return null;
        }
        NetworkEntity network = this.networkDao.findById(component.getNetworkId());
        if (network == null) {
            return null;
        }
        CloudConfig cloudConfig = new CloudConfig();
        List<ComponentInitialization> builders = componentInitializations.stream().filter(builder -> builder.isSupport(component.getComponentType())).collect(Collectors.toList());
        builders.sort(Comparator.comparingInt(ComponentInitialization::getOrder));
        builders.forEach(builder -> {
            builder.initialize(cloudConfig, guest, network, component, componentGuest);
        });
        if (ObjectUtils.isEmpty(cloudConfig.getUserData()) && ObjectUtils.isEmpty(cloudConfig.getNetworks())) {
            return null;
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        String userData = "#cloud-config\n" + yaml.dump(cloudConfig.getUserData());
        String networkData = "#cloud-config\n" + yaml.dump(cloudConfig.getNetworks());
        String metaData = "#cloud-config\n" + yaml.dump(cloudConfig.getMetaData());


        CloudInitIsoGenerator cloudInitIsoGenerator = new CloudInitIsoGenerator();
        String data = cloudInitIsoGenerator.generateCloudInitImage(userData, metaData, "", networkData);
        return CloudData.builder().data(data).waiting(true).build();
    }

    @Override
    public int getSupportType() {
        return Constant.GuestType.COMPONENT;
    }
}
