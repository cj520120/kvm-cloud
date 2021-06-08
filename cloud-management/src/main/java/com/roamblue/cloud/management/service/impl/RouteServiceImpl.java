package com.roamblue.cloud.management.service.impl;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.bean.ClusterInfo;
import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.NetworkEntity;
import com.roamblue.cloud.management.data.entity.VmEntity;
import com.roamblue.cloud.management.service.NetworkAllocateService;
import com.roamblue.cloud.management.service.RouteService;
import com.roamblue.cloud.management.service.VncService;
import com.roamblue.cloud.management.util.VMType;
import com.roamblue.cloud.management.util.IpCaculate;
import com.roamblue.cloud.management.util.IpType;
import com.roamblue.cloud.management.util.TemplateType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class RouteServiceImpl extends AbstractSystemVmService implements RouteService {


    @Autowired
    protected List<NetworkAllocateService> networkAllocateService;
    @Autowired
    private VncService vncService;

    @Override
    protected VmNetworkInfo allocateNetwork(NetworkInfo network, int vmId) {
        Optional<NetworkAllocateService> optional = networkAllocateService.stream().filter(t -> t.getType().equals(network.getType())).findAny();
        NetworkAllocateService allocateService = optional.orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "不支持的网络类型" + network.getType()));
        VmNetworkInfo managerAddress = allocateService.allocateManagerAddress(network.getId(), vmId);
        return managerAddress;
    }

    @Override
    public String getType() {
        return VMType.ROUTE;
    }

    @Override
    protected String getTemplateType() {
        return TemplateType.ROUTE;
    }

    @Override
    protected String getVmDescription(ClusterInfo clusterInfo, NetworkEntity networkInfo) {
        return "Route VM";
    }

    @Override
    protected void onBeforeStart(VmEntity vm, HostEntity host) {

    }

    @Override
    protected void onDestroy(VmEntity vm) {
        this.vncService.unRegister(vm.getClusterId(), vm.getId());
    }

    @Override
    protected void onStop(VmEntity vm) {
        this.vncService.unRegister(vm.getClusterId(), vm.getId());
    }

    private void initializeDHCP(VmEntity vm, HostEntity host) {
        List<NetworkInfo> networks = this.networkService.listNetworkByClusterId(vm.getClusterId());
        if (networks.isEmpty()) {
            throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "无法开启路由:网络未找到");
        }
        StringBuilder dhcp = new StringBuilder();
        dhcp.append("ddns-update-style none;\r\n").append("ignore client-updates;\r\n");
        for (int i = 0; i < networks.size(); i++) {
            NetworkInfo networkInfo = networks.get(i);
            List<VmNetworkInfo> allInstance = this.networkService.listVmNetworkByNetworkId(networkInfo.getId());
            if (!allInstance.isEmpty()) {
                dhcp.append(String.format("subnet %s netmask %s {\r\n", networkInfo.getSubnet().split("/")[0], IpCaculate.getNetMask(networkInfo.getSubnet().split("/")[1])));
                dhcp.append(String.format("  range %s %s;\r\n", networkInfo.getGuestStartIp(), networkInfo.getGuestEndIp()));
                dhcp.append("  option domain-name \"internal.example.org\";\r\n");
                dhcp.append(String.format("  option routers %s;\r\n", networkInfo.getGateway()));
                dhcp.append(String.format("  option broadcast-address %s;\r\n", IpCaculate.getBroadcastAddr(networkInfo.getSubnet())));
                dhcp.append("  default-lease-time 600;\r\n");
                dhcp.append("  max-lease-time 7200;\r\n");
                dhcp.append(String.format("  option domain-name-servers %s;\r\n", networkInfo.getDns()));
                dhcp.append("  group{\r\n");
                for (VmNetworkInfo instanceNetworkEntity : allInstance) {
                    if (instanceNetworkEntity.getType().equals(IpType.GUEST)) {
                        dhcp.append(String.format("    host vm-network-%d{\r\n", instanceNetworkEntity.getId()));
                        dhcp.append(String.format("       hardware ethernet %s;\r\n", instanceNetworkEntity.getMac()));
                        dhcp.append(String.format("       fixed-address %s;\r\n", instanceNetworkEntity.getIp()));
                        dhcp.append("    }\r\n");
                    }
                }
                dhcp.append("  }\r\n");
                dhcp.append("}");
            }
        }
        ResultUtil<Void> resultUtil = this.agentService.writeFile(host.getHostUri(), vm.getVmName(), "/etc/dhcp/dhcpd.conf", dhcp.toString());
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(resultUtil.getCode(), resultUtil.getMessage());
        }
        ResultUtil<Map<String, Object>> restartDhcpResultUtil = this.agentService.execute(host.getHostUri(), vm.getVmName(), "systemctl restart dhcpd");
        if (restartDhcpResultUtil.getCode() != ErrorCode.SUCCESS) {
            throw new CodeException(restartDhcpResultUtil.getCode(), restartDhcpResultUtil.getMessage());
        }

    }

    @Override
    protected void onAfterStart(VmEntity vm, HostEntity host) {
        this.vncService.register(vm.getClusterId(), vm.getId(), host.getHostIp(), vm.getVncPort(), vm.getVncPassword());
        super.initializeNetwork(vm, host);
        this.initializeDHCP(vm, host);
        log.info("Route 启动完成");
    }
}
