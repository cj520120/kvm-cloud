package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.SyncHostTaskIdOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class TaskIdSyncRunner extends AbstractRunner {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private TaskService taskService;


    @Override
    public int getPeriodSeconds() {

        return configService.getConfig(ConfigKey.DEFAULT_TASK_HOST_TASK_SYNC_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : hostList) {
            if (Objects.equals(host.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE)) {
                BaseOperateParam operateParam = SyncHostTaskIdOperate.builder().hostId(host.getHostId()).id(UUID.randomUUID().toString()).title("同步主机任务列表").build();
                this.taskService.addTask(operateParam);
            }
        }
    }

    @Override
    protected String getName() {
        return "检测主机任务列表";
    }

}
