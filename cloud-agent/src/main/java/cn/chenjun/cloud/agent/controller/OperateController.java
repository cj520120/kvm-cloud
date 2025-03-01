package cn.chenjun.cloud.agent.controller;

import cn.chenjun.cloud.agent.annotation.SignRequire;
import cn.chenjun.cloud.agent.operate.OperateDispatch;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenjun
 */
@RestController
public class OperateController {
    @Autowired
    private OperateDispatch dispatch;
    @Autowired
    private ClientService clientService;


    @PostMapping("/api/init")
    public ResultUtil<Void> initHost(@RequestParam("managerUri") String managerUri,
                                     @RequestParam("clientId") String clientId,
                                     @RequestParam("clientSecret") String clientSecret) {
        return this.clientService.init(managerUri, clientId, clientSecret);
    }

    @SignRequire
    @PostMapping("/api/operate")
    public ResultUtil<Object> submitTask(@RequestParam("data") String data) {
        return dispatch.dispatch(data);
    }

}
