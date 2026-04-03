package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.ClusterSignRequire;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.websocket.manager.HostClientManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * @author chenjun
 */
@RestController
public class ClusterController {

    @ClusterSignRequire
    @PostMapping("/api/cluster/host/forward")
    public ResultUtil<Void> report(@RequestParam("hostId") int hostId, @RequestParam("command") int command, @RequestParam("data") String data) {
        HostClientManager.send(hostId, command, data.getBytes(StandardCharsets.UTF_8));
        return ResultUtil.<Void>builder().code(ErrorCode.AGENT_TASK_ASYNC_WAIT).build();
    }
}
