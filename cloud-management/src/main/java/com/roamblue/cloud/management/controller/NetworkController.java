package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.NetworkInfo;
import com.roamblue.cloud.management.bean.VmNetworkInfo;
import com.roamblue.cloud.management.ui.NetworkUiService;
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
@Api(tags = "网络管理")
@Slf4j
public class NetworkController {
    @Autowired
    private NetworkUiService networkUiService;

    @Login
    @GetMapping("/management/network")
    @ApiOperation(value = "获取网络列表")
    public ResultUtil<List<NetworkInfo>> listNetworks() {
        return networkUiService.listNetworks();
    }

    @Login
    @GetMapping("/management/network/search")
    @ApiOperation(value = "根据集群获取网络列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID")
    })
    public ResultUtil<List<NetworkInfo>> search(@RequestParam("clusterId") int clusterId) {
        return networkUiService.search(clusterId);
    }

    @Login
    @GetMapping("/management/network/vm")
    @ApiOperation(value = "获取实例网络信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vmId", value = "实例ID")
    })
    public ResultUtil<List<VmNetworkInfo>> findInstanceNetworkByVmId(int vmId) {
        return networkUiService.findInstanceNetworkByVmId(vmId);
    }

    @Login
    @GetMapping("/management/network/info")
    @ApiOperation(value = "获取网络信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "网络ID")
    })

    public ResultUtil<NetworkInfo> findNetworkById(@RequestParam("id") int id) {
        return networkUiService.findNetworkById(id);
    }

    @Login
    @PostMapping("/management/network/create")
    @ApiOperation(value = "创建网络")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "网络名称"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "managerStartIp", value = "管理分配开始IP"),
            @ApiImplicitParam(name = "managerEndIp", value = "管理分配结束IP"),
            @ApiImplicitParam(name = "guestStartIp", value = "Guest分配开始IP"),
            @ApiImplicitParam(name = "guestEndIp", value = "Guest分配结束IP"),
            @ApiImplicitParam(name = "subnet", value = "子网信息", example = "192.168.2.0/24"),
            @ApiImplicitParam(name = "gateway", value = "网关", example = "192.168.2.1"),
            @ApiImplicitParam(name = "dns", value = "dns信息", example = "192.168.2.1,192.168.1.2,8.8.8.8"),
            @ApiImplicitParam(name = "card", value = "网卡名称", example = "br0"),
            @ApiImplicitParam(name = "type", value = "网络类型", example = "Bridge"),
    })
    public ResultUtil<NetworkInfo> createNetwork(@RequestParam("name") String name,
                                                 @RequestParam("clusterId") int clusterId,
                                                 @RequestParam("guestStartIp") String guestStartIp,
                                                 @RequestParam("guestEndIp") String guestEndIp,
                                                 @RequestParam("managerStartIp") String managerStartIp,
                                                 @RequestParam("managerEndIp") String managerEndIp,
                                                 @RequestParam("subnet") String subnet,
                                                 @RequestParam("gateway") String gateway,
                                                 @RequestParam("dns") String dns,
                                                 @RequestParam("card") String card,
                                                 @RequestParam("type") String type) {


        return networkUiService.createNetwork(name, clusterId, managerStartIp, managerEndIp, guestStartIp, guestEndIp, subnet, gateway, dns, card, type);
    }

    @Login
    @PostMapping("/management/network/destroy")
    @ApiOperation(value = "销毁网络")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "网络ID")
    })
    public ResultUtil<Void> destroyNetwork(@RequestParam("id") int id) {
        return networkUiService.destroyNetworkById(id);

    }

    @Login
    @PostMapping("/management/network/start")
    @ApiOperation(value = "启用网络")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "网络ID")
    })
    public ResultUtil<NetworkInfo> startNetwork(@RequestParam("id") int id) {
        return networkUiService.startNetwork(id);

    }

    @Login
    @PostMapping("/management/network/pause")
    @ApiOperation(value = "暂停网络")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "网络ID")
    })
    public ResultUtil<NetworkInfo> pauseNetwork(@RequestParam("id") int id) {
        return networkUiService.pauseNetwork(id);

    }

}
