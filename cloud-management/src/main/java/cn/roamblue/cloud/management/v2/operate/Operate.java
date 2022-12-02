package cn.roamblue.cloud.management.v2.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.v2.operate.bean.BaseOperateInfo;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
public interface Operate<T extends BaseOperateInfo, V extends ResultUtil> {
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
     * @param hostId
     * @param param
     * @param resultUtil
     */
    void onCallback(String hostId, T param, V resultUtil);
    /**
     * 获取远程调用结果
     *
     * @return
     */
    Type getCallResultType();
}
