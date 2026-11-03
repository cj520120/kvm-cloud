package cn.ruiyun.cloud.management.controller;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.core.annotation.SignRequire;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.servcie.HostService;
import cn.ruiyun.cloud.management.servcie.TaskService;
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
    private TaskService taskService;
    @Autowired
    private HostService hostService;

    @SignRequire
    @PostMapping("/api/agent/task/report")
    public ResultUtil<Void> report(@RequestParam("taskId") String taskId, @RequestParam("data") String data) {
        taskService.submitTaskFinish(taskId, data);
        return ResultUtil.success();
    }

    @SignRequire
    @PostMapping("/api/agent/register")
    public ResultUtil<Void> report(@RequestParam("clientId") String clientId) {
        HostEntity host = this.hostService.getHostInfoByClientId(clientId);
        hostService.registerHost(host.getHostId());
        return ResultUtil.success();
    }
}
