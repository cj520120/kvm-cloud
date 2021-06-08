package com.roamblue.cloud.agent.controller;

import com.roamblue.cloud.agent.service.KvmHostService;
import com.roamblue.cloud.common.agent.HostModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "KVM主机信息")
@Slf4j
public class HostController {
    @Autowired
    private KvmHostService hostService;

    @GetMapping("/host/info")
    @ApiOperation(value = "获取主机信息")
    public ResultUtil<HostModel> getHostInfo() {
        return ResultUtil.<HostModel>builder().data(hostService.getHostInfo()).build();
    }
}
