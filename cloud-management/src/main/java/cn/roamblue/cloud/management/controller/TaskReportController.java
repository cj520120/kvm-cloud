package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.task.OperateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskReportController {
    @Autowired
    private OperateTask operateTask;

    @PostMapping("/api/task/report")
    public ResultUtil<Void> report(
//            @RequestParam("clientId") String clientId,
            @RequestParam("taskId") String taskId,
            @RequestParam("data") String data
//                                   @RequestParam("nonce") String nonce,
//                                   @RequestParam("sign") String sign
    ) {
        operateTask.onTaskFinish(taskId, data);
        return ResultUtil.success();

    }
}
