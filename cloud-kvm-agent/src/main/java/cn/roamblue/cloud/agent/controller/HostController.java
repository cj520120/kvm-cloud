package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.KvmHostService;
import cn.roamblue.cloud.common.agent.HostModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * KVM主机信息
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class HostController {
    @Autowired
    private KvmHostService hostService;

    /**
     * 获取主机信息
     *
     * @return
     */
    @GetMapping("/host/info")
    public ResultUtil<HostModel> getHostInfo() {
        return ResultUtil.<HostModel>builder().data(hostService.getHostInfo()).build();
    }
}
