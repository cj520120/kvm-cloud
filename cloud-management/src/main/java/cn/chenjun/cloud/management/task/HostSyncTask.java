package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.HostCheckOperate;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class HostSyncTask extends AbstractTask {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    @Lazy
    private OperateTask operateTask;

    @Override
    protected void dispatch() {

        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : hostList) {
            switch (host.getStatus()) {

                case Constant.HostStatus.ONLINE:
                case Constant.HostStatus.OFFLINE:
                        BaseOperateParam operateParam = HostCheckOperate.builder().taskId(UUID.randomUUID().toString()).title("检测主机状态").hostId(host.getHostId()).build();
                        this.operateTask.addTask(operateParam);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected int getPeriodSeconds() {
        return 30;
    }
}
