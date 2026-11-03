package cn.ruiyun.cloud.agent.controller;

import cn.ruiyun.cloud.agent.util.ClientService;
import cn.ruiyun.cloud.common.bean.ResultUtil;
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
    private ClientService clientService;


    @PostMapping("/api/init")
    public ResultUtil<Void> initHost(@RequestParam("managerUri") String managerUri,
                                     @RequestParam("clientId") String clientId,
                                     @RequestParam("clientSecret") String clientSecret) {
        return this.clientService.init(managerUri, clientId, clientSecret);
    }
}
