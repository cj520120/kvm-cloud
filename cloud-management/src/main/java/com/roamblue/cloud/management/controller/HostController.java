package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.HostInfo;
import com.roamblue.cloud.management.ui.HostUiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "主机管理")
@Slf4j
public class HostController {
    @Autowired
    private HostUiService hostUiService;

    @Login
    @GetMapping("/management/host")
    @ApiOperation(value = "获取主机列表")
    public ResultUtil<List<HostInfo>> listHost() {
        return hostUiService.listHost();
    }

    @Login
    @GetMapping("/management/host/search")
    @ApiOperation(value = "根据集群获取主机列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID")
    })
    public ResultUtil<List<HostInfo>> search(@RequestParam("clusterId") int clusterId) {
        return hostUiService.search(clusterId);
    }

    @Login
    @PostMapping("/management/host/info")
    @ApiOperation(value = "查找主机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主机ID")
    })
    public ResultUtil<HostInfo> findHostById(@RequestParam("id") int id) {
        return hostUiService.findHostById(id);

    }

    @Login
    @PostMapping("/management/host/create")
    @ApiOperation(value = "创建主机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "name", value = "主机名称"),
            @ApiImplicitParam(name = "ip", value = "主机地址"),
            @ApiImplicitParam(name = "uri", value = "主机agent完整地址")
    })
    public ResultUtil<HostInfo> createHost(@RequestParam("clusterId") int clusterId,
                                           @RequestParam("name") String name,
                                           @RequestParam("ip") String ip,
                                           @RequestParam("uri") String uri) {

        return hostUiService.createHost(clusterId, name, ip, uri);

    }

    @Login
    @PostMapping("/management/host/destroy")
    @ApiOperation(value = "销毁主机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主机ID")
    })
    public ResultUtil<Void> destroyHostById(@RequestParam("id") int id) {
        return hostUiService.destroyHostById(id);
    }

}
