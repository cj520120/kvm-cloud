package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class TaskIdSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = 10;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private OperateTask operateTask;


    @Override
    protected int getPeriodSeconds() {
        return 2;
    }

    @Override
    protected void dispatch() {
        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.TASK_ID_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : hostList) {
                if (Objects.equals(host.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE)) {
                    Map<String, Object> map = new HashMap<>(3);
                    map.put("command", Constant.Command.CHECK_TASK);
                    map.put("taskId", UUID.randomUUID().toString());
                    map.put("data", "{}");
                    try {
                        String uri = String.format("%s/api/operate", host.getUri());
                        String response = HttpUtil.post(uri, map);
                        ResultUtil<List<String>> resultUtil = GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<List<String>>>() {
                        }.getType());
                        List<String> taskIds = resultUtil.getData();
                        if (taskIds != null) {
                            for (String taskId : taskIds) {
                                operateTask.keepTask(taskId);
                            }
                        }
                    } catch (Exception err) {
                        log.error("同步主机任务失败.hostId={}", host.getHostId(), err);
                    }
                }
            }
        }
    }
}
