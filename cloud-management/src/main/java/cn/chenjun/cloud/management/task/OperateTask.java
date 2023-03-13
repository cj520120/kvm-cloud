package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.operate.OperateEngine;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateTask extends AbstractTask {
    private final static int TASK_TIMEOUT_SECONDS = 30;
    @Autowired
    @Qualifier("workExecutorService")
    private ScheduledExecutorService workExecutor;

    @Autowired
    private RedissonClient redis;
    @Autowired
    private OperateEngine operateEngine;


    public void addTask(BaseOperateParam operateParam) {
        redis.getMap(RedisKeyUtil.OPERATE_TASK_KEY).put(operateParam.getTaskId(), operateParam);
    }

    public void keepTask(String taskId) {
        String key = String.format(RedisKeyUtil.OPERATE_TASK_KEEP, taskId);
        redis.getBucket(key).set(System.currentTimeMillis(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected int getPeriodSeconds() {
        return 1;
    }

    @Override
    protected void dispatch() throws Exception {
        RMap<String, BaseOperateParam> rMap = redis.getMap(RedisKeyUtil.OPERATE_TASK_KEY);
        for (Map.Entry<String, BaseOperateParam> entry : rMap.entrySet()) {
            String taskId = entry.getKey();
            String key = String.format(RedisKeyUtil.OPERATE_TASK_KEEP, taskId);
            RBucket<Long> taskBucket = redis.getBucket(key);
            if (taskBucket.isExists()) {
                continue;
            }
            String lockKey = key + ".lock";
            RLock lock = redis.getLock(lockKey);
            if (lock.isLocked()) {
                continue;
            }
            boolean isLock = false;
            try {
                isLock = lock.tryLock(1, TimeUnit.MILLISECONDS);
                if (isLock) {
                    taskBucket.set(System.currentTimeMillis(), TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            } finally {
                if (isLock && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            if (isLock) {
                workExecutor.submit(() -> {
                    try {
                        this.operateEngine.process(entry.getValue());
                    } catch (Exception err) {
                        ResultUtil resultUtil;
                        if (err instanceof CodeException) {
                            CodeException codeException = (CodeException) err;
                            resultUtil = ResultUtil.error(codeException.getCode(), codeException.getMessage());
                        } else {
                            log.error("调用任务出现未知错误.param={}", entry.getValue(), err);
                            resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                        }
                        this.onTaskFinish(entry.getValue().getTaskId(), GsonBuilderUtil.create().toJson(resultUtil));
                    }
                });
            }
        }

    }

    public void onTaskFinish(String taskId, String result) {
        RMap<String, BaseOperateParam> rMap = redis.getMap(RedisKeyUtil.OPERATE_TASK_KEY);
        BaseOperateParam operateParam = rMap.get(taskId);
        rMap.remove(taskId);
        if (operateParam != null) {
            workExecutor.submit(() -> {
                try {
                    this.operateEngine.onFinish(operateParam, result);
                } catch (Exception err) {
                    log.error("任务回调失败.param={} result={}", operateParam, result, err);
                }
            });
        }
    }


}
