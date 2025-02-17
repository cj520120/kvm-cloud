package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.SyncHostTaskIdOperate;
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
public class TaskIdSyncTask extends AbstractTask {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private OperateTask operateTask;


    @Override
    protected int getPeriodSeconds() {
        return 5;
    }

    @Override
    protected void dispatch() {
        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : hostList) {
            if (Objects.equals(host.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE)) {
                BaseOperateParam operateParam = SyncHostTaskIdOperate.builder().hostId(host.getHostId()).taskId(UUID.randomUUID().toString()).title("同步主机任务列表").build();
                this.operateTask.addTask(operateParam);
            }
        }
    }

    @Override
    protected String getName() {
        return "检测主机任务列表";
    }

    @Override
    protected boolean canRunning() {
        return !this.operateTask.hasTask(SyncHostTaskIdOperate.class);
    }
}
