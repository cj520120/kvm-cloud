package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.KvmHostService;
import cn.roamblue.cloud.common.bean.HostInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * KVM主机信息
 *
 * @author chenjun
 */
@RestController
public class HostController {
    @Autowired
    private KvmHostService hostService;

    /**
     * 获取主机信息
     *
     * @return
     */
    @GetMapping("/host/info")
    public ResultUtil<HostInfo> getHostInfo() {
        return ResultUtil.<HostInfo>builder().data(hostService.getHostInfo()).build();
    }
}
