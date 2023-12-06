package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;

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
     * @return
     */
    ResultUtil<?> dispatch(String taskId, String command, String data);


    /**
     * 操作分发
     *
     * @param data
     * @return
     */
    ResultUtil<Void> submitTask(String data);
}
