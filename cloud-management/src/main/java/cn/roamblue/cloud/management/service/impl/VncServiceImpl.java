package cn.roamblue.cloud.management.service.impl;

import cn.hutool.crypto.digest.MD5;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.ClusterInfo;
import cn.roamblue.cloud.management.bean.NetworkInfo;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;
import cn.roamblue.cloud.management.bean.VncInfo;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.VncMapper;
import cn.roamblue.cloud.management.service.NetworkAllocateService;
import cn.roamblue.cloud.management.service.VncService;
import cn.roamblue.cloud.management.util.TemplateType;
import cn.roamblue.cloud.management.util.VmStatus;
import cn.roamblue.cloud.management.util.VmType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
@Slf4j
public class VncServiceImpl extends AbstractSystemVmService implements VncService {

    @Autowired
    protected List<NetworkAllocateService> networkAllocateService;
    @Autowired
    private VncMapper vncMapper;
    @Autowired
    private HostMapper hostMapper;

    @Override
    public String getType() {
        return VmType.CONSOLE;
    }

    @Override
    protected String getTemplateType() {
        return TemplateType.CONSOLE;
    }

    @Override
    protected String getVmDescription(ClusterInfo clusterInfo, NetworkEntity networkInfo) {
        return "Console VM";
    }

    @Override
    protected VmNetworkInfo allocateNetwork(NetworkInfo network, int vmId) {
        Optional<NetworkAllocateService> optional = networkAllocateService.stream().filter(t -> t.getType().equals(network.getType())).findAny();
        NetworkAllocateService allocateService = optional.orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "不支持的网络类型" + network.getType()));
        VmNetworkInfo managerAddress = allocateService.allocateManagerAddress(network.getId(), vmId);
        log.info("system VM[Console] allocate network success,VM={} IP={} MAC={} Device={}", vmId, managerAddress.getIp(), managerAddress.getMac(), managerAddress.getDevice());
        return managerAddress;
    }

    @Override
    protected void onAfterStart(VmEntity vm, HostEntity host) {
        this.register(vm.getClusterId(), vm.getId(), host.getHostIp(), vm.getVncPort(), vm.getVncPassword());
        super.initializeNetwork(vm, host);
        List<VmNetworkInfo> networks = this.networkService.findVmNetworkByVmId(vm.getId());
        List<Integer> networkIds = networks.stream().map(VmNetworkInfo::getNetworkId).distinct().collect(Collectors.toList());
        for (Integer networkId : networkIds) {
            writeVncConfig(vm, networkId, host);
        }
        log.info("system VM[Console] start complete");
    }

    @Override
    protected void onBeforeStart(VmEntity vm, HostEntity host) {
    }

    @Override
    protected void onStop(VmEntity vm) {
        this.unRegister(vm.getClusterId(), vm.getId());
    }

    @Override
    protected void onDestroy(VmEntity vm) {
        this.unRegister(vm.getClusterId(), vm.getId());
    }

    private ResultUtil<Void> writeVncConfig(VmEntity vncInstance, int networkId, HostEntity host) {
        List<VncEntity> vncList = this.vncMapper.findByClusterIdAndNetwork(vncInstance.getClusterId(), networkId);
        StringBuilder vnc = new StringBuilder();
        for (VncEntity vncEntity : vncList) {
            String token = MD5.create().digestHex(String.valueOf(vncEntity.getVmId()));
            String body = token + ": " + vncEntity.getVncHost() + ":" + vncEntity.getVncPort();
            vnc.append(body).append("\r\n");
        }
        String fileName = "/opt/websockify/token/vnc";
        ResultUtil<Void> vncResultUtil = this.agentService.writeFile(host.getHostUri(), vncInstance.getVmName(), fileName, vnc.toString());
        return vncResultUtil;
    }


    @Override
    public ResultUtil<Void> register(int clusterId, int vmId, String host, int port, String password) {

        this.unRegister(clusterId, vmId);
        log.info("register vnc.vm={} vnc=vnc://{}:{}", vmId, host, port);
        List<VmNetworkInfo> networks = this.networkService.findVmNetworkByVmId(vmId);
        List<Integer> networkIds = networks.stream().map(VmNetworkInfo::getNetworkId).distinct().collect(Collectors.toList());
        for (Integer networkId : networkIds) {
            VncEntity vnc = this.vncMapper.findByVmIdAndNetwork(vmId, networkId);
            if (vnc == null) {
                vnc = VncEntity.builder().vncHost(host).vncPort(port).networkId(networkId).vncPassword(password).vmId(vmId).clusterId(clusterId).build();
                this.vncMapper.insert(vnc);
            } else {
                vnc.setVncHost(host);
                vnc.setVncPort(port);
                vnc.setNetworkId(networkId);
                vnc.setVncPassword(password);
                this.vncMapper.updateById(vnc);
            }
            SystemVmEntity systemVm = systemVmMapper.findByNetworkIdAndVmType(networkId, this.getType());
            if (systemVm != null) {
                VmEntity instance = this.vmMapper.selectById(systemVm.getVmId());
                if (instance != null && instance.getVmStatus().equals(VmStatus.RUNNING)) {
                    HostEntity vmHost = hostMapper.selectById(instance.getHostId());
                    if (vmHost != null) {
                        writeVncConfig(instance, networkId, vmHost);
                    }
                }
            }
        }
        return ResultUtil.<Void>builder().build();
    }

    @Override
    public ResultUtil<Void> unRegister(int clusterId, int vmId) {
        try {
            log.info("unregister vnc.vm={}", vmId);
            this.vncMapper.deleteByVmId(vmId);
            return ResultUtil.<Void>builder().build();
        } catch (Exception err) {
            log.error("unregister vnc error.", err);
            return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message(err.getMessage()).build();
        }
    }

    @Override
    public VncInfo findVncByVmId(Integer clusterId, Integer vmId) {
        List<VncEntity> list = this.vncMapper.findByVmId(vmId);
        if (list.isEmpty()) {
            throw new CodeException(ErrorCode.VM_NOT_START, "虚拟机未启动");
        }
        Map<Integer, List<VncEntity>> map = list.stream().collect(Collectors.groupingBy(VncEntity::getNetworkId));
        List<Integer> networkIds = list.stream().map(VncEntity::getNetworkId).distinct().collect(Collectors.toList());
        for (Integer networkId : networkIds) {
            SystemVmEntity systemVm = systemVmMapper.findByNetworkIdAndVmType(networkId, this.getType());
            if (systemVm == null) {
                continue;
            }
            VmEntity vm = vmMapper.selectById(systemVm.getVmId());
            if (vm == null || !vm.getVmStatus().equals(VmStatus.RUNNING)) {
                continue;
            }
            VncEntity vnc = map.get(networkId).get(0);
            String token = MD5.create().digestHex(String.valueOf(vmId));
            return VncInfo.builder().password(vnc.getVncPassword()).ip(vm.getVmIp()).token(token).build();
        }
        throw new CodeException(ErrorCode.VM_NOT_START, "Console实例未就绪");
    }
}
