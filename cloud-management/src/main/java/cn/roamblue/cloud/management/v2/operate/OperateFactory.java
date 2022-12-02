package cn.roamblue.cloud.management.v2.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.operate.bean.BaseOperateInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
public class OperateFactory {
    private final static ServiceLoader<Operate> loader = ServiceLoader.load(Operate.class);

    public static void create(BaseOperateInfo param) {
        Iterator<Operate> iterator = loader.iterator();
        while (iterator.hasNext()) {
            Operate operate = iterator.next();
            if (operate.getParamType().equals(param.getClass())) {
                ThreadPoolExecutor executor = SpringContextUtils.getBean(ThreadPoolExecutor.class);
                try {
                    executor.submit(() -> operate.operate(param));
                }catch (CodeException err){
                    executor.submit(()->operate.onCallback("",param, ResultUtil.error(err.getCode(),err.getMessage())));
                }catch (Exception err){
                    executor.submit(()->operate.onCallback("",param, ResultUtil.error(ErrorCode.SERVER_ERROR,"执行出错")));
                }
            }
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + param.getClass());
    }
    public static void onCallback(BaseOperateInfo param,String hostId,String result){
        Iterator<Operate> iterator = loader.iterator();
        while (iterator.hasNext()) {
            Operate operate = iterator.next();
            if (operate.getParamType().equals(param.getClass())) {
                ThreadPoolExecutor executor = SpringContextUtils.getBean(ThreadPoolExecutor.class);
                try {
                    ResultUtil resultUtil= GsonBuilderUtil.create().fromJson(result,operate.getCallResultType());
                    executor.submit(() -> operate.onCallback(hostId,param,resultUtil));
                }catch (Exception err){
                    log.error("任务回调出错.",err);
                }
            }
        }
    }
}
