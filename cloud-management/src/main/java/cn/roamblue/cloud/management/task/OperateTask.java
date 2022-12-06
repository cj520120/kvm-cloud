package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.operate.OperateEngine;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class OperateTask implements CommandLineRunner {
    private final static int TASK_TIMEOUT = 3;
    @Autowired
    @Qualifier("workExecutorService")
    private ScheduledExecutorService workExecutor;
    @Autowired
    @Qualifier("bossExecutorService")
    private ScheduledExecutorService bossExecutor;
    @Autowired
    private RedissonClient redis;
    @Autowired
    private OperateEngine operateEngine;


    public void addTask(BaseOperateParam operateParam) {
        redis.getMap(RedisKeyUtil.OPERATE_TASK_KEY).put(operateParam.getTaskId(), operateParam);
    }

    public void keepTask(String taskId) {
        String key = String.format(RedisKeyUtil.OPERATE_TASK_KEEP, taskId);
        redis.getBucket(key).set(System.currentTimeMillis(), TASK_TIMEOUT, TimeUnit.MINUTES);
    }

    public void dispatch() {
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
            try {
                if (lock.tryLock(1, TimeUnit.SECONDS)) {
                    taskBucket.set(System.currentTimeMillis(), TASK_TIMEOUT, TimeUnit.MINUTES);

                    workExecutor.submit(() -> {
                        try {
                            this.operateEngine.process(entry.getValue());
                        } catch (Exception err) {
                            ResultUtil resultUtil;
                            if (err instanceof CodeException) {
                                CodeException codeException = (CodeException) err;
                                resultUtil = ResultUtil.error(codeException.getCode(), codeException.getMessage());
                            } else {
                                resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                            }
                            this.onTaskFinish(entry.getValue().getTaskId(), GsonBuilderUtil.create().toJson(resultUtil));
                        }
                    });
                }
            } catch (InterruptedException err) {
                //do nothing
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

    }

    public void onTaskFinish(String taskId, String result) {
        RMap<String, BaseOperateParam> rMap = redis.getMap(RedisKeyUtil.OPERATE_TASK_KEY);
        BaseOperateParam operateParam = rMap.get(taskId);
        rMap.remove(taskId);
        if (operateParam != null) {
            workExecutor.submit(() -> {
                this.operateEngine.onFinish(operateParam, result);
            });
        }
    }

    @Override
    public void run(String... args) throws Exception {
        this.bossExecutor.scheduleAtFixedRate(this::dispatch, 10, 1, TimeUnit.SECONDS);
    }
}
