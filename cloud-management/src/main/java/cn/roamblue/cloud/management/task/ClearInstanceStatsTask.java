package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.data.entity.VmStaticsEntity;
import cn.roamblue.cloud.management.data.mapper.VmStatsMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * 清理过期的实例监控信息
 *
 * @author chenjun
 */
@Component
public class ClearInstanceStatsTask extends AbstractTask {
    @Autowired
    private VmStatsMapper vmStatsMapper;

    @Override
    protected int getInterval() {
        return 10 * 60 * 1000;
    }

    @Override
    protected String getName() {
        return "ClearInstanceStatsTask";
    }

    @Override
    protected void call() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);
        QueryWrapper<VmStaticsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("create_time", c.getTime());
        vmStatsMapper.delete(queryWrapper);
    }
}