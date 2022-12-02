package cn.roamblue.cloud.management.v2.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.operate.bean.BaseOperateInfo;
import cn.roamblue.cloud.management.v2.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public class OperateFactory {
    private final static ServiceLoader<Operate> loader = ServiceLoader.load(Operate.class);
    private final static int TASK_TIMEOUT = 3;


    public static void submitTask(BaseOperateInfo param) {
        RedisTemplate redis = SpringContextUtils.getBean(RedisTemplate.class);
        redis.opsForHash().put(RedisKeyUtil.OPERATE_TASK_KEY, param.getTaskId(), param);
    }

    public static void dispatch() {
        RedisTemplate redis = SpringContextUtils.getBean(RedisTemplate.class);
        Cursor<Map.Entry<String, BaseOperateInfo>> cursor = redis.opsForHash().scan(RedisKeyUtil.OPERATE_TASK_KEY, ScanOptions.scanOptions().count(10).build());
        while (cursor.hasNext()) {
            Map.Entry<String, BaseOperateInfo> entry = cursor.next();
            String taskId = entry.getKey();
            String key = String.format(RedisKeyUtil.OPERATE_TASK_KEEP, taskId);
            if (redis.hasKey(key)) {
                continue;
            }
            if (redis.opsForValue().setIfAbsent(key, System.currentTimeMillis(), 1, TimeUnit.MINUTES)) {
                create(entry.getValue());
                break;
            }
        }

    }

    public static void keep(String taskId) {
        RedisTemplate redis = SpringContextUtils.getBean(RedisTemplate.class);
        String key = String.format(RedisKeyUtil.OPERATE_TASK_KEEP, taskId);
        redis.opsForValue().set(key, System.currentTimeMillis(), OperateFactory.TASK_TIMEOUT, TimeUnit.MINUTES);
    }

    public static void create(BaseOperateInfo param) {
        Iterator<Operate> iterator = loader.iterator();
        while (iterator.hasNext()) {
            Operate operate = iterator.next();
            if (operate.getParamType().equals(param.getClass())) {
                ThreadPoolExecutor executor = SpringContextUtils.getBean(ThreadPoolExecutor.class);
                executor.submit(() -> {
                    try {
                        operate.operate(param);
                    } catch (CodeException err) {
                        operate.onSubmitCallback(param.getTaskId(), ResultUtil.error(err.getCode(), err.getMessage()));
                    } catch (Exception err) {
                        operate.onSubmitCallback(param.getTaskId(), ResultUtil.error(ErrorCode.SERVER_ERROR, "执行出错"));
                    }
                });

            }
        }
        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + param.getClass());
    }

    public static void onCallback(String taskId, String hostId, String result) {
        RedisTemplate redis = SpringContextUtils.getBean(RedisTemplate.class);
        BaseOperateInfo operateParam = (BaseOperateInfo) redis.opsForHash().get(RedisKeyUtil.OPERATE_TASK_KEY, taskId);
        redis.opsForHash().delete(RedisKeyUtil.OPERATE_TASK_KEY, taskId);
        if (operateParam != null) {
            Iterator<Operate> iterator = loader.iterator();
            while (iterator.hasNext()) {
                Operate operate = iterator.next();
                if (operate.getParamType().equals(operateParam.getClass())) {
                    ThreadPoolExecutor executor = SpringContextUtils.getBean(ThreadPoolExecutor.class);
                    try {
                        ResultUtil resultUtil = GsonBuilderUtil.create().fromJson(result, operate.getCallResultType());
                        executor.submit(() -> operate.onCallback(hostId, operateParam, resultUtil));
                    } catch (Exception err) {
                        log.error("任务回调出错.", err);
                    }
                }
            }
        }
    }
}
