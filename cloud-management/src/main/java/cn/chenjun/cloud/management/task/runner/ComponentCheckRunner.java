package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class ComponentCheckRunner extends AbstractRunner {
    static final List<Integer> NETWORK_STATUS_CHECK_LIST = Arrays.asList(Constant.NetworkStatus.READY, Constant.NetworkStatus.INSTALL);

    @Autowired
    private NetworkService networkService;
    @Autowired
    private HostService hostService;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private LockRunner lockRunner;

    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(ConfigKey.DEFAULT_TASK_COMPONENT_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() throws Exception {
        log.debug("开始检测系统组件...");
        List<NetworkEntity> networkList = networkService.listNetwork().stream().filter(network -> NETWORK_STATUS_CHECK_LIST.contains(network.getStatus())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(networkList)) {
            log.warn("没有找到需要检测的网络，等待网络添加后继续...");
            return;
        }
        List<HostEntity> hostList = this.hostService.listAllHost();
        if (ObjectUtils.isEmpty(hostList)) {
            log.warn("没有找到任何主机，等待系统组件主机添加后继续...");
            return;
        }
        List<HostEntity> masterHostList = hostList.stream().filter(host -> HostRole.isMaster(host.getRole())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(masterHostList)) {
            log.warn("没有找到任何主节点，等待系统组件主机添加后继续...");
            return;
        }
        for (NetworkEntity network : networkList) {
            if (!this.componentService.checkNetworkIsEnableComponent(network.getNetworkId())) {
                continue;
            }
            if (network.getStatus() == Constant.NetworkStatus.MAINTENANCE) {
                log.warn("网络处于维护状态，等待网络就绪后继续...");
                continue;
            }
            List<ComponentEntity> componentList = this.componentService.listComponentByNetworkId(network.getNetworkId());
            for (ComponentEntity component : componentList) {
                boolean isComponentReady = false;
                try {
                    isComponentReady = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), () -> this.componentService.checkNetworkComponentReady(masterHostList, component, network));

                } catch (Exception e) {
                    log.error("检测网络[{}]失败", network.getNetworkId(), e);
                }
                if (isComponentReady) {
                    try {
                        lockRunner.lockRun(RedisKeyUtil.getGlobalLockKey(), () -> this.componentService.cleanOldComponentGuest(component.getComponentId(), masterHostList.stream().map(HostEntity::getHostId).collect(Collectors.toList())));
                    } catch (Exception e) {
                        log.error("清理组件虚拟机失败,component_id={}", component.getComponentId());
                    }
                    try {
                        this.componentService.cleanUnlinkComponentGuest(component.getComponentId());
                    } catch (Exception e) {
                        log.error("清理未关联的组件虚拟机失败,component_id={}", component.getComponentId());
                    }
                }

                for (HostEntity host : hostList) {
                    if (HostRole.isMaster(host.getRole())) {
                        try {
                            lockRunner.lockRun(RedisKeyUtil.getGlobalLockKey(), () -> this.componentService.checkAndCreateMissComponentGuest(component.getComponentId(), host.getHostId()));

                        } catch (CodeException err) {
                            log.error("安装系统组件失败.componentId={} msg={},跳出安装检测...", component.getComponentId(), err.getMessage());
                            break;
                        } catch (Exception e) {
                            log.error("安装系统组件失败,component_id={}", component.getComponentId(), e);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "系统组件检测";
    }


}
