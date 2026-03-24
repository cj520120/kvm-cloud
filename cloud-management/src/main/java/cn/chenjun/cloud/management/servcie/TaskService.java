package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.dao.TaskDao;
import cn.chenjun.cloud.management.data.entity.TaskEntity;
import cn.chenjun.cloud.management.servcie.bean.OperateFinishBean;
import cn.chenjun.cloud.management.util.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class TaskService {

    @Autowired
    protected ConfigService configService;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private ApplicationContext applicationContext;

    @Transactional(rollbackFor = Exception.class)
    public void addTask(BaseOperateParam operateParam) {
        this.addTask(operateParam, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addTask(BaseOperateParam operateParam, int delayMinute) {
        TaskEntity task = this.findTask(operateParam.getTaskId());
        if (task == null) {
            task = TaskEntity.builder().taskId(operateParam.getTaskId())
                    .version(0)
                    .title(operateParam.getTitle())
                    .type(operateParam.getClass().getName())
                    .param(GsonBuilderUtil.create().toJson(operateParam))
                    .expireTime(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinute)))
                    .createTime(new Date(System.currentTimeMillis()))
                    .build();
            taskDao.insert(task);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void keepTask(String taskId) {
        int expire = configService.getConfig(ConfigKey.DEFAULT_TASK_EXPIRE_TIMEOUT_SECOND);
        taskDao.keep(taskId, new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expire)));
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TaskEntity> listCanRunTask(int count) {
        return this.taskDao.listCanRunTask(count);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean startTask(TaskEntity entity) {
        int expireSecond = this.configService.getConfig(ConfigKey.DEFAULT_TASK_EXPIRE_TIMEOUT_SECOND);
        Date expireTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSecond));
        return this.taskDao.updateVersion(entity.getTaskId(), entity.getVersion(), expireTime) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(String taskId) {
        this.taskDao.deleteById(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TaskEntity findTask(String taskId) {
        return this.taskDao.findById(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitTaskFinish(String taskId, String result) {
        TaskEntity task = this.findTask(taskId);
        if (task == null) {
            log.error("无效的任务Id:{},result={}", taskId, result);
            return;
        }
        this.applicationContext.publishEvent(OperateFinishBean.builder().taskId(taskId).operateType(task.getType()).param(task.getParam()).result(result).build());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delayTask(TaskEntity entity) {
        int nextDelaySecond = this.configService.getConfig(ConfigKey.DEFAULT_TASK_WAIT_DELAY_SECOND);
        Date delayTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(nextDelaySecond));
        return this.taskDao.updateVersion(entity.getTaskId(), entity.getVersion(), delayTime) > 0;
    }
}
