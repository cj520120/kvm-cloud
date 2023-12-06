package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.SyncHostGuestOperate;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class HostGuestSyncTask extends AbstractTask {

    @Autowired
    private HostMapper hostMapper;
    @Autowired
    @Lazy
    private OperateTask operateTask;

    @Override
    protected int getPeriodSeconds() {
        return (int) TimeUnit.SECONDS.toSeconds(30);
    }

    @Override
    protected void dispatch() {
        List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
        for (HostEntity host : hostList) {
            if (host.getStatus() == Constant.HostStatus.ONLINE) {
                BaseOperateParam operate = SyncHostGuestOperate.builder().hostId(host.getHostId()).title("同步主机客户机信息").taskId(UUID.randomUUID().toString()).build();
                this.operateTask.addTask(operate);
            }
        }
    }
}
