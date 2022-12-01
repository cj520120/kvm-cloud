package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;

/**
 * @author chenjun
 */
public interface OperateDispatch {

    /**
     * 操作分发
     * @param command
     * @param data
     * @return
     * @param <T>
     */
    <T> ResultUtil<T> dispatch(String command,String data);
}
