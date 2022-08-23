package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.agent.VmInfoModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.*;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.data.entity.SystemVmEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.data.mapper.SystemVmMapper;
import cn.roamblue.cloud.management.util.IpCaculate;
import cn.roamblue.cloud.management.util.NetworkStatus;
import cn.roamblue.cloud.management.util.VmStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractSystemVmService extends AbstractVmService {
    @Autowired
    protected SystemVmMapper systemVmMapper;
    @Autowired
    protected NetworkMapper networkMapper;

    @Override
    public VmInfo resume(int vmId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public VmInfo changeCdRoom(int id, int isoTemplateId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public VolumeInfo attachDisk(int vmId, int volumeId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public VolumeInfo detachDisk(int vmId, int volumeId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public VmInfo modify(int vmId, String description, int calculationSchemeId, int groupId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public TemplateInfo createTemplate(int vmId, String name) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    @Override
    public VmInfo reInstall(int vmId, int templateId,int storageId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    /**
     * 初始化网卡信息
     *
     * @param instance
     * @param host
     */
    protected void initializeNetwork(VmEntity instance, HostEntity host) {
        List<NetworkInfo> networks = this.networkService.listNetworkByClusterId(instance.getClusterId());
        if (networks.isEmpty()) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        List<VmNetworkInfo> vmNetworkInfoList = this.networkService.findVmNetworkByVmId(instance.getId());
        if (vmNetworkInfoList.isEmpty()) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
        }
        Map<Integer, NetworkInfo> networkInfoMap = networks.stream().collect(Collectors.toMap(NetworkInfo::getId, Function.identity()));
        for (int i = 0; i < vmNetworkInfoList.size(); i++) {
            VmNetworkInfo vmNetworkInfo = vmNetworkInfoList.get(i);
            NetworkInfo networkInfo = networkInfoMap.get(vmNetworkInfo.getNetworkId());
            if (networkInfo == null) {
                throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "网络不存在");
            }
            if (!networkInfo.getStatus().equals(NetworkStatus.READY)) {
                throw new CodeException(ErrorCode.NETWORK_NOT_READY, "网络不存在");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("TYPE=Ethernet\r\n")
                    .append("BROWSER_ONLY=no\r\n")
                    .append("BOOTPROTO=static\r\n")
                    .append("DEFROUTE=yes\r\n")
                    .append("IPV4_FAILURE_FATAL=no\r\n")
                    .append("NAME=eth").append(vmNetworkInfo.getDevice()).append("\r\n")
                    .append("DEVICE=eth").append(vmNetworkInfo.getDevice()).append("\r\n")
                    .append("ONBOOT=yes\r\n")
                    .append(String.format("IPADDR=%s\r\n", vmNetworkInfo.getIp()))
                    .append(String.format("NETMASK=%s\r\n", IpCaculate.getNetMask(networkInfo.getSubnet().split("/")[1])))
                    .append(String.format("GATEWAY=%s\r\n", networkInfo.getGateway()));
            String[] dns = networkInfo.getDns().split(",");
            for (int dnsIndex = 0; dnsIndex < dns.length; dnsIndex++) {
                sb.append("DNS").append(dnsIndex + 1).append("=").append(dns[dnsIndex]).append("\r\n");
            }
            String filePath = "/etc/sysconfig/network-scripts/ifcfg-eth" + vmNetworkInfo.getDevice();
            String networkConfig = sb.toString();
            log.info("写入系统VM网络配置[{}].name=eth{},config={}", this.getType(), vmNetworkInfo.getDevice(), networkConfig);
            do {
                ResultUtil<Void> resultUtil = this.agentService.writeFile(host.getHostUri(), instance.getVmName(), filePath, networkConfig);
                if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                    log.info("系统虚拟机[{}]写入网络配置成功,name=eth{},IP={}", this.getType(), vmNetworkInfo.getDevice(), vmNetworkInfo.getIp());
                    break;
                } else if (resultUtil.getCode() == ErrorCode.VM_NOT_FOUND || resultUtil.getCode() == ErrorCode.AGENT_VM_NOT_FOUND) {
                    throw new CodeException(ErrorCode.VM_NOT_START, "系统虚拟机[" + this.getType() + "]网络初始化失败。虚拟机未启动");
                }
                log.info("等待系统虚拟机启动.Type=[{}].name=eth{},config={}", this.getType(), vmNetworkInfo.getDevice(), networkConfig);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            } while (true);
        }

        ResultUtil<Map<String, Object>> restartNetworkResultUtil = this.agentService.execute(host.getHostUri(), instance.getVmName(), "systemctl restart network");
        if (restartNetworkResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(restartNetworkResultUtil.getCode(), restartNetworkResultUtil.getMessage());
        }
    }

    public void start(int clusterId) {
        ClusterInfo clusterInfo = this.clusterService.findClusterById(clusterId);
    	log.info("{}[{}]开始检测系统虚拟机状态.Type={}",clusterInfo.getName(),clusterInfo.getId(),  this.getType());
        List<Integer> templateIds = this.templateService.listTemplateByClusterId(clusterId).stream().filter(t -> t.getType().equals(this.getTemplateType())).map(TemplateInfo::getId).collect(Collectors.toList());
        if (templateIds.isEmpty()) {
            log.warn("{}[{}]不能初始化系统VM [{}]，系统模版未找到",clusterInfo.getName(),clusterInfo.getId(), this.getType());
            return;
        }

        List<NetworkEntity> networks = networkMapper.findByClusterId(clusterId);
        for (NetworkEntity network : networks) {
            if (!network.getNetworkStatus().equals(NetworkStatus.READY)) {
                continue;
            }
            try {
                SystemVmEntity systemVmEntity = systemVmMapper.findByNetworkIdAndVmType(network.getId(), this.getType());
                if (systemVmEntity != null) {
                    VmEntity instance = this.vmMapper.selectById(systemVmEntity.getVmId());
                    if (instance == null) {
                        systemVmMapper.deleteById(systemVmEntity.getId());
                        log.warn("{}[{}]检测系统虚拟机无效，删除无效数据。准备重新创建虚拟机.Type={} Network={}",clusterInfo.getName(),clusterInfo.getId(),  this.getType(), network.getId());
                        continue;
                    } else if (instance.getVmStatus().equals(VmStatus.STOPPED)) {
                        super.startVm(instance.getId(), 0);
                    } else if (instance.getVmStatus().equals(VmStatus.RUNNING)||instance.getVmStatus().equals(VmStatus.STARING)) {

                        log.debug("{}[{}]开始检测系统VM运行状态.Type={} Network={} HostId={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), network.getId(), instance.getHostId());
                        HostInfo hostInfo = this.hostService.findHostById(instance.getHostId());
                        ResultUtil<VmInfoModel> resultUtil = this.agentService.getInstance(hostInfo.getUri(), instance.getVmName());
                        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                            log.debug("{}集群[{}]系统虚拟机成功运行.Type={} Network={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), network.getId());
                        } else if (resultUtil.getCode() == ErrorCode.AGENT_VM_NOT_FOUND) {
                            log.warn("{}集群[{}]系统虚拟机未运行,准备重启。 Type={} Network={} VM={}", clusterInfo.getName(),clusterInfo.getId(),this.getType(), network.getId(), instance.getVmName());
                            super.reboot(instance.getId(), true);
                        } else {
                            log.error("{}集群[{}]无法检测系统VM运行状态: Type={} Network={} msg={}", clusterInfo.getName(),clusterInfo.getId(),this.getType(), network.getId(), resultUtil.getMessage());
                        }
                    } else {
                        log.warn("{}集群[{}]无法检测系统VM运行状态：未知状态.Type={} Network={} status={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), network.getId(), instance.getVmStatus());
                    }
                } else {
                    log.info("{}集群[{}]开始创建系统虚拟机.Type={} Network={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), network.getId());
                    String description = this.getVmDescription(clusterInfo, network);
                    int templateId = templateIds.get(0);
                    long diskSize = 0L;
                    int groupId = 0;
                    systemVmEntity = SystemVmEntity.builder()
                            .vmId(0)
                            .vmType(this.getType())
                            .networkId(network.getId())
                            .createTime(new Date())
                            .build();
                    systemVmMapper.insert(systemVmEntity);
                    VmEntity instance = createVm(description, 0, clusterId, 0, templateId, diskSize, network.getId(), this.getType(), groupId);
                    systemVmEntity.setVmId(instance.getId());
                    systemVmMapper.updateById(systemVmEntity);
                    super.startVm(instance.getId(), 0);
                    log.info("{}集群[{}]启动系统虚拟机成功. Type={} Network={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), network.getId());

                }
            } catch (CodeException e) {
                log.error("{}集群[{}]无法初始化系统虚拟机.type={} msg={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), e.getMessage());

            } catch (Exception e) {
                log.error("{}集群[{}]无法初始化系统虚拟机.type={}",clusterInfo.getName(),clusterInfo.getId(), this.getType(), e);
            }
        }
    }

    /**
     * 获取虚拟机名称
     *
     * @param clusterInfo
     * @param networkInfo
     * @return
     */
    protected abstract String getVmDescription(ClusterInfo clusterInfo, NetworkEntity networkInfo);

    /**
     * 获取模版类型
     *
     * @return
     */
    protected abstract String getTemplateType();

}
