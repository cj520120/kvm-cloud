package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.operate.OperateDispatch;
import cn.roamblue.cloud.common.bean.ResultUtil;
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
    @PostMapping("/api/operate")
    public <T> ResultUtil<T> execute(@RequestParam("command") String command,@RequestParam("data") String data) {
       return dispatch.dispatch(command,data);
    }
}
