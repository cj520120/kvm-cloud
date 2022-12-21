package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.bean.ResultUtil;

/**
 * @author chenjun
 */
public interface OperateDispatch {

    /**
     * 操作分发
     *
     * @param taskId
     * @param command
     * @param data
     * @param <T>
     * @return
     */
    <T> ResultUtil<T> dispatch(String taskId, String command, String data);
}
