package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.SignRequire;
import cn.chenjun.cloud.management.model.HostModel;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.task.OperateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenjun
 */
@RestController
public class AgentController {
    @Autowired
    private OperateTask operateTask;
    @Autowired
    private HostService hostService;

    @SignRequire
    @PostMapping("/api/agent/task/report")
    public ResultUtil<Void> report(
            @RequestParam("taskId") String taskId,
            @RequestParam("data") String data
    ) {

        operateTask.onTaskFinish(taskId, data);
        return ResultUtil.success();
    }

    @SignRequire
    @PostMapping("/api/agent/register")
    public ResultUtil<Void> report(@RequestParam("clientId") String clientId
    ) {

        ResultUtil<HostModel> resultUtil = this.hostService.getHostInfoByClientId(clientId);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return ResultUtil.<Void>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).build();
        }
        hostService.registerHost(resultUtil.getData().getHostId());
        return ResultUtil.success();
    }
}
