package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.TaskEntity;
import cn.chenjun.cloud.management.data.mapper.TaskMapper;
import cn.chenjun.cloud.management.operate.OperateEngine;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateTask extends AbstractTask {
    private final static int TASK_TIMEOUT_SECONDS = 30;


    @Autowired
    private OperateEngine operateEngine;

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private RedissonClient redissonClient;

    public void addTask(BaseOperateParam operateParam) {
        TaskEntity task = TaskEntity.builder().taskId(operateParam.getTaskId())
                .version(0)
                .title(operateParam.getTitle())
                .type(operateParam.getClass().getName())
                .param(GsonBuilderUtil.create().toJson(operateParam))
                .expireTime(new Date(System.currentTimeMillis()))
                .createTime(new Date(System.currentTimeMillis()))
                .build();
        taskMapper.insert(task);
    }

    @Override
    protected boolean canRunning() {
        return true;
    }

    public boolean hasTask(Class taskType) {
        return this.taskMapper.selectCount(new QueryWrapper<TaskEntity>().eq(TaskEntity.TASK_TYPE, taskType.getName())) > 0;
    }

    public void addTask(BaseOperateParam operateParam, int delayMinute) {
        TaskEntity task = TaskEntity.builder().taskId(operateParam.getTaskId())
                .version(0)
                .title(operateParam.getTitle())
                .type(operateParam.getClass().getName())
                .param(GsonBuilderUtil.create().toJson(operateParam))
                .expireTime(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinute)))
                .createTime(new Date(System.currentTimeMillis()))
                .build();
        taskMapper.insert(task);
    }

    public void keepTask(String taskId) {
        taskMapper.keep(taskId, new Date(System.currentTimeMillis() + +TimeUnit.SECONDS.toMillis(TASK_TIMEOUT_SECONDS)));
    }

    @Override
    protected int getPeriodSeconds() {
        return 1;
    }

    @Override
    protected void dispatch() throws Exception {
        QueryWrapper<TaskEntity> wrapper = new QueryWrapper<TaskEntity>().lt(TaskEntity.EXPIRE_TIME, new Date(System.currentTimeMillis()));
        wrapper.last("limit 0,10");
        List<TaskEntity> taskList = this.taskMapper.selectList(wrapper);
        for (TaskEntity entity : taskList) {
            try {
                Date expireTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TASK_TIMEOUT_SECONDS));
                if (this.taskMapper.updateVersion(entity.getTaskId(), entity.getVersion(), expireTime) > 0) {
                    Class<BaseOperateParam> paramClass = (Class<BaseOperateParam>) Class.forName(entity.getType());
                    BaseOperateParam operateParam = GsonBuilderUtil.create().fromJson(entity.getParam(), paramClass);
                    this.lockRun(() -> {
                        this.operateEngine.process(operateParam);
                    });
                }
            } catch (Exception err) {
                ResultUtil<?> resultUtil;
                if (err instanceof CodeException) {
                    CodeException codeException = (CodeException) err;
                    resultUtil = ResultUtil.error(codeException.getCode(), codeException.getMessage());
                } else {
                    log.error("调用任务出现未知错误.param={}", entity.getParam(), err);
                    resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                }
                this.onTaskFinish(entity.getTaskId(), GsonBuilderUtil.create().toJson(resultUtil));
            }
        }

    }

    public void onTaskFinish(String taskId, String result) {
        TaskEntity task = this.taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        try {
            Class<BaseOperateParam> paramClass = (Class<BaseOperateParam>) Class.forName(task.getType());
            BaseOperateParam operateParam = GsonBuilderUtil.create().fromJson(task.getParam(), paramClass);
            try {
                this.lockRun(() -> {
                    this.operateEngine.onFinish(operateParam, result);
                });
                this.taskMapper.deleteById(taskId);
            } catch (Exception err) {
                log.error("任务回调失败.param={} result={}", operateParam, result, err);
            }
        } catch (Exception err) {
            log.error("解析任务参数出错:task={} result={}", task, result);
        }

    }

    private void lockRun(Runnable runnable) {
        RLock rLock = redissonClient.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY);
        try {
            rLock.lock(1, TimeUnit.MINUTES);
            runnable.run();
        } finally {
            try {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    protected String getName() {
        return "检测可执行任务";
    }
}
