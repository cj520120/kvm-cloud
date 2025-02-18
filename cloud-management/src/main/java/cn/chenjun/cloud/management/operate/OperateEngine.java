package cn.chenjun.cloud.management.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateEngine {

    @Autowired
    private PluginRegistry<Operate, Integer> operatePluginRegistry;

    @Autowired
    private LockRunner runner;


    @Transactional(rollbackFor = Exception.class)
    public void onFinish(BaseOperateParam operateParam, String result) {
        runner.lockRun(RedisKeyUtil.GLOBAL_RW_LOCK_KET, true, () -> {
            log.info("onFinish type={} param={} result={}", operateParam.getClass().getName(), operateParam, result);
            Optional<Operate> optional = this.operatePluginRegistry.getPluginFor(operateParam.getType());
            optional.ifPresent(operate -> {
                ResultUtil<?> resultUtil;
                try {
                    resultUtil = GsonBuilderUtil.create().fromJson(result, operate.getCallResultType());
                } catch (Exception err) {
                    resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                }
                operate.onComplete(operateParam, resultUtil);
            });
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(BaseOperateParam operateParam) {
        runner.lockRun(RedisKeyUtil.GLOBAL_RW_LOCK_KET, false, () -> {
            log.info("process type={} param={}", operateParam.getClass().getName(), operateParam);
            Optional<Operate> optional = this.operatePluginRegistry.getPluginFor(operateParam.getType());
            optional.ifPresent(operate -> operate.process(operateParam));
        });

    }
}
