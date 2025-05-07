package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.model.SimpleNetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class NetworkController extends BaseController {
    @Autowired
    private NetworkService networkService;

    @GetMapping("/api/network/info")
    public ResultUtil<NetworkModel> getNetworkInfo(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.getNetworkInfo(networkId));
    }

    @GetMapping("/api/network/search")
    public ResultUtil<Page<SimpleNetworkModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                 @RequestParam("no") int no,
                                                 @RequestParam("size") int size) {
        return this.lockRun(() -> networkService.search(keyword, no, size));
    }
    @GetMapping("/api/network/all")
    public ResultUtil<List<SimpleNetworkModel>> listNetwork() {
        return this.lockRun(() -> networkService.listNetwork());
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PutMapping("/api/network/create")
    public ResultUtil<NetworkModel> createNetwork(@RequestParam("name") String name,
                                                  @RequestParam("startIp") String startIp,
                                                  @RequestParam("endIp") String endIp,
                                                  @RequestParam(value = "gateway", defaultValue = "") String gateway,
                                                  @RequestParam("mask") String mask,
                                                  @RequestParam(value = "bridge", defaultValue = "") String bridge,
                                                  @RequestParam("subnet") String subnet,
                                                  @RequestParam("broadcast") String broadcast,
                                                  @RequestParam("dns") String dns,
                                                  @RequestParam("domain") String domain,
                                                  @RequestParam("type") int type,
                                                  @RequestParam("bridgeType") int bridgeType,
                                                  @RequestParam(value = "vlanId", defaultValue = "0") int vlanId,
                                                  @RequestParam(value = "basicNetworkId", defaultValue = "0") int basicNetworkId) {
        return this.lockRun(() -> networkService.createNetwork(name, startIp, endIp, gateway, mask, subnet, broadcast, bridge, dns, domain, type, vlanId, basicNetworkId, bridgeType));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PostMapping("/api/network/register")
    public ResultUtil<NetworkModel> registerNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.registerNetwork(networkId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PostMapping("/api/network/maintenance")
    public ResultUtil<NetworkModel> maintenanceNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.maintenanceNetwork(networkId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/network/destroy")
    public ResultUtil<NetworkModel> destroyNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.destroyNetwork(networkId));
    }
}
