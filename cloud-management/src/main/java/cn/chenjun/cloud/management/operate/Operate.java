package cn.chenjun.cloud.management.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import org.springframework.plugin.core.Plugin;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
public interface Operate extends Plugin<Integer> {


    /**
     * 操作
     *
     * @param param
     */
    void process(BaseOperateParam param);


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
