package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class ComponentCheckRunner extends AbstractRunner {

    @Autowired
    protected NotifyService notifyService;
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    private NetworkMapper networkMapper;
    @Autowired
    private PluginRegistry<ComponentProcess, Integer> processPluginRegistry;
    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private HostMapper hostMapper;
    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(ConfigKey.DEFAULT_TASK_COMPONENT_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            List<ConfigQuery> queryList = new ArrayList<>();
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.common.util.Constant.ConfigType.NETWORK).id(network.getNetworkId()).build());
            boolean isCheckComponentEnable = Objects.equals(this.configService.getConfig(queryList, ConfigKey.SYSTEM_COMPONENT_ENABLE), cn.chenjun.cloud.common.util.Constant.Enable.YES);
            List<HostEntity> hostList = this.hostMapper.selectList(new QueryWrapper<>());
            switch (network.getStatus()) {
                case Constant.NetworkStatus.READY:
                case Constant.NetworkStatus.INSTALL:
                    break;
                default:
                    continue;
            }
            if (!isCheckComponentEnable) {
                if (network.getStatus() != cn.chenjun.cloud.common.util.Constant.NetworkStatus.INSTALL) {
                    network.setStatus(Constant.NetworkStatus.READY);
                    networkMapper.updateById(network);
                    this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                    return;
                }
            }
            hostList.sort(Comparator.comparingInt(HostEntity::getHostId));
            int size = hostList.size();
            List<ComponentEntity> components = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, network.getNetworkId()));
            for (ComponentEntity component : components) {
                boolean componentReady = false;
                for (int i = 0; i < size; i++) {
                    HostEntity host = hostList.get(i);
                    if (!Objects.equals(host.getStatus(), cn.chenjun.cloud.common.util.Constant.HostStatus.ONLINE)) {
                        continue;
                    }
                    boolean isMaster = i == 0;
                    Optional<ComponentProcess> optional = processPluginRegistry.getPluginFor(component.getComponentType());
                    if (optional.isPresent()) {
                        ComponentProcess process = optional.get();
                        try {
                            if (!HostRole.isComponent(host.getRole())) {
                                process.cleanHostComponent(component, host);
                            } else {
                                boolean isReady = process.checkAndStart(network, component, host, isMaster);
                                componentReady |= isReady;

                            }
                        } catch (Exception e) {
                            log.error("组件检测失败: {}", component.getComponentType(), e);
                        }
                    }
                }
                if (Objects.equals(Constant.ComponentType.ROUTE, component.getComponentType())) {
                    if (componentReady && !Objects.equals(Constant.NetworkStatus.READY, network.getStatus())) {
                        network.setStatus(Constant.NetworkStatus.READY);
                        networkMapper.updateById(network);
                        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                    } else if (!componentReady && !Objects.equals(Constant.NetworkStatus.INSTALL, network.getStatus())) {
                        network.setStatus(Constant.NetworkStatus.INSTALL);
                        networkMapper.updateById(network);
                        this.notifyService.publish(NotifyData.<Void>builder().id(network.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_NETWORK).build());
                    }
                }
            }
        }
    }

    @Override
    protected String getName() {
        return "系统组件检测";
    }

}
