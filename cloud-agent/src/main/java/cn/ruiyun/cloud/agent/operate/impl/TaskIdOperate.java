package cn.ruiyun.cloud.agent.operate.impl;

import cn.ruiyun.cloud.agent.util.TaskPoolUtil;
import cn.ruiyun.cloud.common.bean.NoneRequest;
import cn.ruiyun.cloud.common.core.annotation.DispatchBind;
import cn.ruiyun.cloud.common.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class TaskIdOperate {

    @DispatchBind(command = Constant.Command.CHECK_TASK,async = false)
    public List<String> checkTask(Connect connect, NoneRequest request) {
        return TaskPoolUtil.getAllTaskIds();
    }
}
