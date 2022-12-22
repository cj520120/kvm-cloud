package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.AppUtils;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.model.HostModel;
import cn.roamblue.cloud.management.servcie.HostService;
import cn.roamblue.cloud.management.task.OperateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class AgentController {
    @Autowired
    private OperateTask operateTask;
    @Autowired
    private HostService hostService;

    @PostMapping("/api/agent/task/report")
    public ResultUtil<Void> report(@RequestParam("clientId") String clientId,
                                   @RequestParam("taskId") String taskId,
                                   @RequestParam("data") String data,
                                   @RequestParam("nonce") String nonce,
                                   @RequestParam("sign") String sign
    ) {
        Map<String, Object> map = new HashMap<>(5);
        map.put("taskId", taskId);
        map.put("clientId", clientId);
        map.put("data", data);
        map.put("nonce", nonce);
        ResultUtil<HostModel> resultUtil = this.hostService.getHostInfoByClientId(clientId);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return ResultUtil.<Void>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).build();
        }
        try {
            String dataSign = AppUtils.sign(map, clientId, resultUtil.getData().getClientSecret(), nonce);
            if (!Objects.equals(dataSign, sign)) {
                return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message("数据签名错误").build();
            }
        } catch (Exception e) {
            return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message("签名错误").build();
        }
        operateTask.onTaskFinish(taskId, data);
        return ResultUtil.success();
    }

    @PostMapping("/api/agent/register")
    public ResultUtil<Void> report(@RequestParam("clientId") String clientId,
                                   @RequestParam("nonce") String nonce,
                                   @RequestParam("sign") String sign
    ) {
        Map<String, Object> map = new HashMap<>(5);
        map.put("clientId", clientId);
        map.put("nonce", nonce);
        ResultUtil<HostModel> resultUtil = this.hostService.getHostInfoByClientId(clientId);
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return ResultUtil.<Void>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).build();
        }
        try {
            String dataSign = AppUtils.sign(map, clientId, resultUtil.getData().getClientSecret(), nonce);
            if (!Objects.equals(dataSign, sign)) {
                return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message("数据签名错误").build();
            }
        } catch (Exception e) {
            return ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message("签名错误").build();
        }
        hostService.registerHost(resultUtil.getData().getHostId());
        return ResultUtil.success();
    }
}
