package cn.roamblue.cloud.management.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.operate.bean.BaseOperateParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void onFinish(BaseOperateParam operateParam, String result) {
        log.info("onFinish type={} param={} result={}", operateParam.getClass().getName(), operateParam, result);
        Operate operate = this.operateHandlerMap.get(operateParam.getClass());
        if (operate == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的任务:" + operate.getParamType());
        }
        ResultUtil resultUtil;
        try {
            resultUtil = GsonBuilderUtil.create().fromJson(result, operate.getCallResultType());
        } catch (Exception err) {
            resultUtil = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        }
        operate.onFinish(operateParam, resultUtil);
    }

    public void process(BaseOperateParam operateParam) {
        log.info("process type={} param={}", operateParam.getClass().getName(), operateParam);
        Operate operate = this.operateHandlerMap.get(operateParam.getClass());
        if (operate == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的任务:" + operate.getParamType());
        }
        operate.operate(operateParam);

    }


}
