package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.data.entity.VmEntity;
import cn.roamblue.cloud.management.data.mapper.VmMapper;
import cn.roamblue.cloud.management.service.LockService;
import cn.roamblue.cloud.management.service.NetworkService;
import cn.roamblue.cloud.management.service.VncService;
import cn.roamblue.cloud.management.service.VolumeService;
import cn.roamblue.cloud.management.util.LockKeyUtil;
import cn.roamblue.cloud.management.util.VmStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 销毁超过等待期的Vm
 *
 * @author chenjun
 */
@Slf4j
@Component
public class VmDestroyTask extends AbstractTask {
    @Autowired
    private VmMapper vmMapper; 
    @Autowired
    private NetworkService networkService;
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private VncService vncService;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return this.config.getVmDestroyCheckInterval();
    }

    @Override
    protected String getName() {
        return "DestroyInstance";
    }

    @Override
    protected void call() {
        long removeTime = System.currentTimeMillis() - this.config.getVmDestroyExpireSeconds() * 1000L;
        QueryWrapper<VmEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("vm_status", VmStatus.DESTROY);
        wrapper.lt("remove_time", new Date(removeTime));
        List<VmEntity> list = vmMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return;
        }
        for (VmEntity instance : list) {
            lockService.tryRun(LockKeyUtil.getInstanceLockKey(instance.getId()), () -> {
                this.networkService.unBindVmNetworkByVmId(instance.getId());
                this.volumeService.destroyByVmId(instance.getId());
                this.vncService.destroy(instance.getId());
                vmMapper.deleteById(instance.getId());
                log.info("销毁过期虚拟机 vm={}", instance.getVmDescription());
                return null;
            }, 10, TimeUnit.SECONDS);
        }
    }
}
