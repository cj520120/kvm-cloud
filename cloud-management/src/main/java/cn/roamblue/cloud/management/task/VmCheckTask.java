package cn.roamblue.cloud.management.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.roamblue.cloud.common.agent.VmInfoModel;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.service.AgentService;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.VmStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * VM 检测
 *
 * @author chenjun
 */
@Slf4j
@Component
public class VmCheckTask extends AbstractTask {
	@Autowired
	private LockService lockService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private VmMapper vmMapper;
	@Autowired
	private HostMapper hostMapper;

	@Override
	protected int getInterval() {
		return this.config.getVmStatusCheckInterval();
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
					log.warn("unknown VM, auto shutdown.VM={}", vmInfo.getName());
					continue;
				}

				lockService.tryRun(LockKeyUtil.getInstanceLockKey(vm.getId()), () -> {
					switch (vm.getVmStatus()) {
					case VmStatus.RUNNING:
					case VmStatus.STARING:
						if (!vm.getHostId().equals(hostInfo.getId())) {
							log.warn("VM[{}] running host error, auto destroy", vmInfo.getName());
							// 如果运行机器和当前机器不一致，则直接销毁
							agentService.destroyVm(hostInfo.getHostUri(), vm.getVmName());
						}
						break;
					default:
						log.warn("VM[{}] running state is inconsistent, auto destroy", vmInfo.getName());
						// 如果运行机器和当前机器不一致，则直接销毁
						agentService.destroyVm(hostInfo.getHostUri(), vm.getVmName());
						break;
					}
					return null;
				}, 10, TimeUnit.SECONDS);

			}
		}

	}
}
