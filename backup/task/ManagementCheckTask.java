package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.data.entity.ManagementEntity;
import cn.roamblue.cloud.management.data.mapper.ManagementMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 管理端检测清理
 *
 * @author chenjun
 */
@Component
public class ManagementCheckTask extends AbstractTask {
    @Autowired
    private ManagementMapper managementMapper;

    @Override
    protected int getInterval() {
        return this.config.getManagerKeepInterval();
    }

    @Override
    protected String getName() {
        return "ManagementCheckTask";
    }

    @Override
    protected void call() {
        long expire = System.currentTimeMillis() - this.config.getManagerKeepInterval() * 3 * 1000;
        QueryWrapper<ManagementEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("last_active_time", new Date(expire));
        managementMapper.delete(queryWrapper);
    }
}
