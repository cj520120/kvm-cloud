package cn.chenjun.cloud.management.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.core.operate.OperateService;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.servcie.TaskService;
import cn.chenjun.cloud.management.servcie.bean.OperateFinishBean;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateEngine {

    @Autowired
    private PluginRegistry<OperateService, Integer> operatePluginRegistry;
    @Autowired
    private TaskService taskService;
    @Autowired
    private LockRunner lockRunner;
    @Autowired
    @Qualifier("taskExecutorService")
    private ScheduledThreadPoolExecutor executor;

    @Transactional(rollbackFor = Exception.class)
    public void onFinish(BaseOperateParam operateParam, String result) {
        log.info("onFinish type={} param={} result={}", operateParam.getClass().getName(), operateParam, result);
        Optional<OperateService> optional = this.operatePluginRegistry.getPluginFor(operateParam.getType());
        optional.ifPresent(operateService -> {
            ResultUtil<?> resultUtil;
            try {
                resultUtil = GsonBuilderUtil.create().fromJson(result, operateService.getCallResultType());
            } catch (Exception err) {
                resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
            }
            operateService.onComplete(operateParam, resultUtil);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(BaseOperateParam operateParam) {
        log.info("process type={} param={}", operateParam.getClass().getName(), operateParam);
        Optional<OperateService> optional = this.operatePluginRegistry.getPluginFor(operateParam.getType());

        optional.ifPresent(operateService -> {
            if (operateService.requireLock()) {
                lockRunner.lockRun(operateService.getLockKey(operateParam), () -> {
                    operateService.process(operateParam);
                });
            } else {
                operateService.process(operateParam);
            }
        });
    }

    @EventListener
    public <T> void onOperateFinish(OperateFinishBean<T> operateFinishBean) {
        this.executor.submit(() -> lockRunner.lockRun(RedisKeyUtil.getGlobalLockKey(), () -> {
            try {
                log.info("任务回调:task={} result={}", operateFinishBean.getTaskId(), operateFinishBean.getResult());
                Class<BaseOperateParam> paramClass = (Class<BaseOperateParam>) Class.forName(operateFinishBean.getOperateType());
                BaseOperateParam operateParam = GsonBuilderUtil.create().fromJson(operateFinishBean.getParam(), paramClass);
                try {
                    this.onFinish(operateParam, operateFinishBean.getResult());
                    this.taskService.deleteTask(operateFinishBean.getTaskId());
                } catch (Exception err) {
                    log.error("任务回调失败.param={} result={}", operateParam, operateFinishBean.getResult(), err);
                }
            } catch (Exception err) {
                log.error("解析任务参数出错:task={} result={}", operateFinishBean.getTaskId(), operateFinishBean.getResult());
            }
        }));
    }
}
