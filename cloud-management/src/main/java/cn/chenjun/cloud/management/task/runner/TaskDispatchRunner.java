package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.TaskEntity;
import cn.chenjun.cloud.management.operate.OperateEngine;
import cn.chenjun.cloud.management.servcie.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class TaskDispatchRunner extends AbstractRunner {
    @Autowired
    private OperateEngine operateEngine;
    @Autowired
    private TaskService taskService;


    @Override
    public int getPeriodSeconds() {
        return 1;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void dispatch() throws Exception {
        List<TaskEntity> taskList = this.taskService.listCanRunTask(100);
        log.debug("检测到可执行任务:{}", taskList.size());
        for (TaskEntity entity : taskList) {
            try {
                if (this.taskService.startTask(entity)) {
                    log.debug("执行任务:{}-{}", entity.getTaskId(), entity.getTitle());
                    Class<BaseOperateParam> paramClass = (Class<BaseOperateParam>) Class.forName(entity.getType());
                    BaseOperateParam operateParam = GsonBuilderUtil.create().fromJson(entity.getParam(), paramClass);
                    this.operateEngine.process(operateParam);
                } else {
                    log.info("任务:{}-{}已经更新,已忽略", entity.getTaskId(), entity.getTitle());
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
                if (resultUtil.getCode() != ErrorCode.WAITING) {
                    log.info("任务:{}-{}延期执行,延期原因:{}", entity.getTaskId(), entity.getTitle(), resultUtil.getMessage());
                    this.taskService.submitTaskFinish(entity.getTaskId(), GsonBuilderUtil.create().toJson(resultUtil));
                } else {
                    this.taskService.delayTask(entity);
                }
            }
        }
    }


    @Override
    public String getName() {
        return "检测可执行任务";
    }
}
