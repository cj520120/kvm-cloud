package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.HostCheckOperate;
import cn.chenjun.cloud.management.servcie.TaskService;
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
public class HostSyncRunner extends AbstractRunner {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private TaskService taskService;

    @Override
    protected void dispatch() {
        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());

        for (HostEntity host : hostList) {
            switch (host.getStatus()) {
                case Constant.HostStatus.ONLINE:
                case Constant.HostStatus.OFFLINE:
                    BaseOperateParam operateParam = HostCheckOperate.builder().id(UUID.randomUUID().toString()).title("检测主机状态").hostId(host.getHostId()).build();
                    this.taskService.addTask(operateParam);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_TASK_HOST_CHECK_TIMEOUT_SECOND);
    }

    @Override
    protected String getName() {
        return "宿主机检测";
    }

}
