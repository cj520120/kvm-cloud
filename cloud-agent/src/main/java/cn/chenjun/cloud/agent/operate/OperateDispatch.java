package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.common.bean.ResultUtil;

/**
 * @author chenjun
 */
public interface OperateDispatch {


    /**
     * 操作分发
     *
     * @param data
     * @return
     */
    ResultUtil<Object> dispatch(String data);
}
