package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.ClusterInfo;
import com.roamblue.cloud.management.ui.ClusterUiService;
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

/**
 * @author chenjun
 */
@RestController
@Api(tags = "集群管理")
@Slf4j
public class ClusterController {
    @Autowired
    private ClusterUiService clusterUiService;

    @Login
    @GetMapping("/management/cluster")
    @ApiOperation(value = "获取集群列表")
    public ResultUtil<List<ClusterInfo>> listCluster() {
        return clusterUiService.listCluster();
    }

    @Login
    @GetMapping("/management/cluster/info")
    @ApiOperation(value = "查找集群")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "集群ID")
    })
    public ResultUtil<ClusterInfo> findClusterById(@RequestParam("id") int id) {
        return clusterUiService.findClusterById(id);
    }

    @Login
    @PostMapping("/management/cluster/create")
    @ApiOperation(value = "创建集群")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "集群名称"),
            @ApiImplicitParam(name = "overCpu", value = "cpu比例"),
            @ApiImplicitParam(name = "overMemory", value = "memory比例")
    })
    public ResultUtil<ClusterInfo> createCluster(@RequestParam("name") String name,
                                                 @RequestParam("overCpu") float overCpu,
                                                 @RequestParam("overMemory") float overMemory) {

        return clusterUiService.createCluster(name, overCpu, overMemory);
    }

    @Login
    @PostMapping("/management/cluster/modify")
    @ApiOperation(value = "修改集群")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id"),
            @ApiImplicitParam(name = "name", value = "集群名称"),
            @ApiImplicitParam(name = "overCpu", value = "cpu比例"),
            @ApiImplicitParam(name = "overMemory", value = "memory比例")
    })
    public ResultUtil<ClusterInfo> modifyCluster(@RequestParam("id") int id,
                                                 @RequestParam("name") String name,
                                                 @RequestParam("overCpu") float overCpu,
                                                 @RequestParam("overMemory") float overMemory) {

        return clusterUiService.modifyCluster(id, name, overCpu, overMemory);
    }

    @Login
    @PostMapping("/management/cluster/destroy")
    @ApiOperation(value = "删除集群")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "集群ID")
    })
    public ResultUtil<Void> destroyClusterById(@RequestParam("id") int id) {
        return clusterUiService.destroyClusterById(id);
    }
}
