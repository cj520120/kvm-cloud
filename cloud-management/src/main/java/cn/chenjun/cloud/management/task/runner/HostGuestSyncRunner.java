package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.SyncHostGuestOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class HostGuestSyncRunner extends AbstractRunner {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private TaskService taskService;

    @Override
    public int getPeriodSeconds() {

        return configService.getConfig(ConfigKey.DEFAULT_TASK_HOST_GUEST_SYNC_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : hostList) {
            if (host.getStatus() == Constant.HostStatus.ONLINE) {
                BaseOperateParam operate = SyncHostGuestOperate.builder().hostId(host.getHostId()).title("同步主机客户机信息").id(UUID.randomUUID().toString()).build();
                this.taskService.addTask(operate);
            }
        }
    }

    @Override
    protected String getName() {
        return "同步主机虚拟机";
    }

}
