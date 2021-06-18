package com.roamblue.cloud.management.service.impl;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.*;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.NetworkEntity;
import com.roamblue.cloud.management.data.entity.SystemVmEntity;
import com.roamblue.cloud.management.data.entity.VmEntity;
import com.roamblue.cloud.management.data.mapper.NetworkMapper;
import com.roamblue.cloud.management.data.mapper.SystemVmMapper;
import com.roamblue.cloud.management.util.VmStatus;
import com.roamblue.cloud.management.util.IpCaculate;
import com.roamblue.cloud.management.util.NetworkStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public VmInfo reInstall(int vmId, int templateId) {
        throw new CodeException(ErrorCode.NOT_SUPPORTED, "系统实例不支持该操作");
    }

    protected void initializeNetwork(VmEntity instance, HostEntity host) {
        long startTime = System.currentTimeMillis();
        List<NetworkInfo> networks = this.networkService.listNetworkByClusterId(instance.getClusterId());
        if (networks.isEmpty()) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "无法初始化[" + this.getType() + "]网络，网络不存在");
        }
        List<VmNetworkInfo> vmNetworkInfoList = this.networkService.findVmNetworkByVmId(instance.getId());
        if (networks.isEmpty()) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "无法初始化[" + this.getType() + "]网络，没有配置IP");
        }
        boolean completed = false;
        do {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            for (int i = 0; i < networks.size(); i++) {
                NetworkInfo networkInfo = networks.get(i);
                if (!networkInfo.getStatus().equals(NetworkStatus.READY)) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                VmNetworkInfo vmNetworkInfo = vmNetworkInfoList.stream().filter(t -> t.getNetworkId() == networkInfo.getId()).findFirst().orElse(null);
                if (vmNetworkInfo == null) {
                    continue;
                }


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
                ResultUtil<Void> resultUtil = this.agentService.writeFile(host.getHostUri(), instance.getVmName(), filePath, sb.toString());
                if (resultUtil.getCode() == ErrorCode.VM_NOT_FOUND) {
                    throw new CodeException(ErrorCode.VM_NOT_START, "[" + this.getType() + "]网络初始化超时.VM未启动");
                } else if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                    completed = false;
                    break;
                } else {
                    completed = true;
                }
            }
            if ((System.currentTimeMillis() - startTime) > 60 * 1000) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "[" + this.getType() + "]网络初始化超时.");
            }
        } while (!completed);

        ResultUtil<Map<String, Object>> restartNetworkResultUtil = this.agentService.execute(host.getHostUri(), instance.getVmName(), "systemctl restart network");
        if (restartNetworkResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(restartNetworkResultUtil.getCode(), restartNetworkResultUtil.getMessage());
        }
    }

    public void start(int clusterId) {
        ClusterInfo clusterInfo = this.clusterService.findClusterById(clusterId);
        List<Integer> templateIds = this.templateService.listTemplateByClusterId(clusterId).stream().filter(t -> t.getType().equals(this.getTemplateType())).map(TemplateInfo::getId).collect(Collectors.toList());
        if (templateIds.isEmpty()) {
            log.warn("无法初始化[{}]，模版不存在", this.getType());
            return;
        }

        List<NetworkEntity> networks = networkMapper.findByClusterId(clusterId);
        for (NetworkEntity network : networks) {
            if(!network.getNetworkStatus().equals(NetworkStatus.READY)){
                continue;
            }
            try {
                SystemVmEntity systemVmEntity = systemVmMapper.findByNetworkIdAndVmType(network.getId(), this.getType());
                if (systemVmEntity != null) {
                    VmEntity instance = this.vmMapper.selectById(systemVmEntity.getVmId());
                    if (instance == null) {
                        systemVmMapper.deleteById(systemVmEntity.getId());
                        log.debug("检测系统VM Type={} Network={} 失败，无效的VM，准备重新创建VM", this.getType(), network.getId());
                        continue;
                    } else if (instance.getVmStatus().equals(VmStatus.STOPPED)) {
                        super.startVm(instance.getId(), 0);
                    } else {
                        log.debug("检测系统VM Type={} Network={} 通过", this.getType(), network.getId());
                    }
                } else {
                    log.info("开始创建系统VM Type={} Network={}", this.getType(), network.getId());
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
                    log.info("启动系统VM Type={} Network={} 成功", this.getType(), network.getId());

                }
            } catch (CodeException e) {
                log.error("初始化系统VM{}失败.msg={}", this.getType(), e.getMessage());

            } catch (Exception e) {
                log.error("初始化系统VM{}失败.", this.getType(), e);
            }
        }
    }

    protected abstract String getVmDescription(ClusterInfo clusterInfo, NetworkEntity networkInfo);

    protected abstract String getTemplateType();

}
