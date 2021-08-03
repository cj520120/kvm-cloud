package cn.roamblue.cloud.management.task;

import cn.hutool.cache.impl.LRUCache;
import cn.roamblue.cloud.common.agent.VmInfoModel;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.entity.VmStaticsEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.data.mapper.VmStatsMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.HostService;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.service.VncService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.VmStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * VM 检测
 *
 * @author chenjun
 */
@Slf4j
@Component
public class VmCheckTask extends AbstractTask {
    @Autowired
    private HostService hostService;
    @Autowired
    private LockService lockService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private VmStatsMapper vmStatsMapper;

    private LRUCache<Integer, VmStaticsEntity> cache = new LRUCache<>(100000, 60000L);
    @Autowired
    private VncService vncService;

    @Override
    protected int getInterval() {
        return 10000;
    }

    @Override
    protected String getName() {
        return "VmCheckTask";
    }

    @Override
    protected void call() {
        List<HostEntity> list = hostMapper.selectAll();
        for (HostEntity hostInfo : list) {
            List<VmInfoModel> vmInfoList = agentService.getInstance(hostInfo.getHostUri()).getData();
            if (vmInfoList == null || vmInfoList.isEmpty()) {
                continue;
            }
            for (VmInfoModel vmInfo : vmInfoList) {
                VmEntity vm = vmMapper.findByName(vmInfo.getName());
                if (vm == null) {
                    agentService.destroyVm(hostInfo.getHostUri(), vmInfo.getName());
                    log.warn("Unknown VM, auto shutdown.name={}", vmInfo.getName());
                    continue;
                }
                lockService.tryRun(LockKeyUtil.getInstanceLockKey(vm.getId()), () -> {
                    if (!vm.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED) && vm.getHostId().equals(hostInfo.getId())) {
                        return null;
                    }
                    int id = vm.getId();
                    VmEntity find = vmMapper.selectById(id);
                    if (find == null) {
                        return null;
                    }
                    if ((System.currentTimeMillis() - find.getLastUpdateTime().getTime()) < 60000) {
                        log.info("ignore VM detection VM-Name={}", vm.getVmDescription());
                        return null;
                    }
                    if (find.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED) || !find.getHostId().equals(hostInfo.getId())) {
                        log.warn("VM state is inconsistent, auto destroy", vmInfo.getName());
                        //如果运行机器和当前机器不一致，则直接销毁
                        agentService.destroyVm(hostInfo.getHostUri(), find.getVmName());
                    }
                    return null;
                }, 10, TimeUnit.SECONDS);

            }
        }

    }
}
