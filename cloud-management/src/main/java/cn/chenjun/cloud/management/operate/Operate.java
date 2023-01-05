package cn.chenjun.cloud.management.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
public interface Operate<T extends BaseOperateParam, V extends ResultUtil> {
    /**
     * 获取操作类型
     *
     * @return
     */
    Class<T> getParamType();

    /**
     * 操作
     *
     * @param param
     */
    void operate(T param);

    /**
     * 结果回调
     *
     * @param param
     * @param resultUtil
     */
    void onFinish(T param, V resultUtil);

    /**
     * 获取远程调用结果
     *
     * @return
     */
    Type getCallResultType();

}
