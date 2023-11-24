package cn.chenjun.cloud.management.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateEngine {
    private final Map<Class, Operate> operateHandlerMap;

    public OperateEngine(@Autowired List<Operate> operates) {
        this.operateHandlerMap = operates.stream().collect(Collectors.toMap(Operate::getParamType, Function.identity()));
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    public void onFinish(BaseOperateParam operateParam, String result) {

        log.info("onFinish type={} param={} result={}", operateParam.getClass().getName(), operateParam, result);
        Operate<BaseOperateParam, ResultUtil<?>> operate = this.operateHandlerMap.get(operateParam.getClass());
        if (operate == null) {
            return;
        }
        ResultUtil<?> resultUtil;
        try {
            resultUtil = GsonBuilderUtil.create().fromJson(result, operate.getCallResultType());
        } catch (Exception err) {
            resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        }
        operate.onFinish(operateParam, resultUtil);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    public void process(BaseOperateParam operateParam) {
        log.info("process type={} param={}", operateParam.getClass().getName(), operateParam);
        Operate<BaseOperateParam, ResultUtil<?>> operate = this.operateHandlerMap.get(operateParam.getClass());
        if (operate == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的任务:" + operateParam.getClass());
        }
        operate.operate(operateParam);

    }


}
