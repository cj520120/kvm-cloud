package cn.chenjun.cloud.common.core.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import org.springframework.plugin.core.Plugin;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
public interface OperateService extends Plugin<Integer> {


    /**
     * 操作
     *
     * @param param
     */
    void process(BaseOperateParam param);

    /**
     * 是否需要锁
     *
     * @return
     */
    boolean requireLock();

    /**
     * 获取锁的key
     *
     * @param param
     * @return
     */
    String getLockKey(BaseOperateParam param);
    /**
     * 结果回调
     *
     * @param param
     * @param resultUtil
     */
    void onComplete(BaseOperateParam param, ResultUtil<?> resultUtil);

    /**
     * 获取远程调用结果
     *
     * @return
     */
    Type getCallResultType();

}
