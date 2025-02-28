package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.data.entity.TaskEntity;
import cn.chenjun.cloud.management.data.mapper.TaskMapper;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.bean.OperateFinishBean;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskService {

    @Autowired
    protected ConfigService configService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Transactional
    public void addTask(BaseOperateParam operateParam) {
        this.addTask(operateParam, 0);
    }

    @Transactional
    public void addTask(BaseOperateParam operateParam, int delayMinute) {
            TaskEntity task = TaskEntity.builder().taskId(operateParam.getTaskId())
                    .version(0)
                    .title(operateParam.getTitle())
                    .type(operateParam.getClass().getName())
                    .param(GsonBuilderUtil.create().toJson(operateParam))
                    .expireTime(new Date(System.currentTimeMillis()+ TimeUnit.MINUTES.toMillis(delayMinute)))
                    .createTime(new Date(System.currentTimeMillis() ))
                    .build();
            taskMapper.insert(task);
    }

    @Transactional
    public void keepTask(String taskId) {
        int expire = configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_TASK_EXPIRE_TIMEOUT_SECOND);
        taskMapper.keep(taskId, new Date(System.currentTimeMillis() + +TimeUnit.SECONDS.toMillis(expire)));
    }

    @Transactional
    public boolean hasTask(Class taskType) {
        return this.taskMapper.selectCount(new QueryWrapper<TaskEntity>().eq(TaskEntity.TASK_TYPE, taskType.getName())) > 0;
    }

    @Transactional
    public List<TaskEntity> listCanRunTask(int count) {
        QueryWrapper<TaskEntity> wrapper = new QueryWrapper<TaskEntity>().lt(TaskEntity.EXPIRE_TIME, new Date(System.currentTimeMillis()));
        wrapper.last("limit 0," + count);
        List<TaskEntity> taskList = this.taskMapper.selectList(wrapper);
        return taskList;
    }

    @Transactional
    public void updateTaskExpire(TaskEntity entity) {
        int expireSecond = this.configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_TASK_EXPIRE_TIMEOUT_SECOND);
        Date expireTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSecond));
        int version=entity.getVersion()+1;
        entity.setVersion(version);
        entity.setExpireTime(expireTime);
        this.taskMapper.updateById(entity);
    }

    @Transactional
    public void deleteTask(String taskId) {
        this.taskMapper.deleteById(taskId);
    }

    @Transactional
    public TaskEntity findTask(String taskId) {
        return this.taskMapper.selectOne(new QueryWrapper<TaskEntity>().eq(TaskEntity.TASK_ID, taskId));
    }

    public void submitTaskFinish(String taskId, String result) {
        TaskEntity task = this.findTask(taskId);
        if (task == null) {
            log.error("无效的任务Id:{},result={}", taskId, result);
            return;
        }
        this.applicationContext.publishEvent(OperateFinishBean.builder().taskId(taskId).operateType(task.getType()).param(task.getParam()).result(result).build());

    }
}
