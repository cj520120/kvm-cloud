package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.ComponentDetailModel;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.model.NetworkModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
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
    public ResultUtil<Page<NetworkModel>> search(@RequestParam(value = "keyword",required = false) String keyword,
                                                 @RequestParam("no") int no,
                                                 @RequestParam("size") int size) {
        return this.lockRun(() -> networkService.search(keyword, no, size));
    }
    @GetMapping("/api/network/all")
    public ResultUtil<List<NetworkModel>> listNetwork() {
        return this.lockRun(() -> networkService.listNetwork());
    }

    @GetMapping("/api/network/component")
    public ResultUtil<List<ComponentDetailModel>> listNetworkComponent(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.listNetworkComponent(networkId));
    }

    @PostMapping("/api/network/component/slave/update")
    public ResultUtil<ComponentDetailModel> updateComponentSlaveNumber(@RequestParam("componentId") int componentId, @RequestParam("number") int number) {
        return this.lockRun(() -> networkService.updateComponentSlaveNumber(componentId, number));
    }

    @PutMapping("/api/network/component/create")
    public ResultUtil<ComponentDetailModel> createComponent(@RequestParam("networkId") int networkId, @RequestParam("componentType") int componentType) {
        return this.lockRun(() -> networkService.createComponent(networkId, componentType));
    }

    @PutMapping("/api/network/create")
    public ResultUtil<NetworkModel> createNetwork(@RequestParam("name") String name,
                                                  @RequestParam("startIp") String startIp,
                                                  @RequestParam("endIp") String endIp,
                                                  @RequestParam("gateway") String gateway,
                                                  @RequestParam("mask") String mask,
                                                  @RequestParam("bridge") String bridge,
                                                  @RequestParam("subnet") String subnet,
                                                  @RequestParam("broadcast") String broadcast,
                                                  @RequestParam("dns") String dns,
                                                  @RequestParam("domain") String domain,
                                                  @RequestParam("type") int type,
                                                  @RequestParam("bridgeType") int bridgeType,
                                                  @RequestParam("vlanId") int vlanId,
                                                  @RequestParam("basicNetworkId") int basicNetworkId) {
        return this.lockRun(() -> networkService.createNetwork(name, startIp, endIp, gateway, mask, subnet, broadcast, bridge, dns, domain, type, vlanId, basicNetworkId, bridgeType));
    }

    @PostMapping("/api/network/register")
    public ResultUtil<NetworkModel> registerNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.registerNetwork(networkId));
    }

    @PostMapping("/api/network/maintenance")
    public ResultUtil<NetworkModel> maintenanceNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.maintenanceNetwork(networkId));
    }

    @DeleteMapping("/api/network/destroy")
    public ResultUtil<NetworkModel> destroyNetwork(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.destroyNetwork(networkId));
    }

    @DeleteMapping("/api/network/component/destroy")
    public ResultUtil<Void> destroyNetworkComponent(@RequestParam("componentId") int componentId) {
        return this.lockRun(() -> networkService.destroyComponent(componentId));
    }

    @GetMapping("/api/network/nat/list")
    public ResultUtil<List<NatModel>> listNetworkComponentNat(@RequestParam("componentId") int componentId) {
        return this.lockRun(() -> networkService.listComponentNat(componentId));
    }

    @PutMapping("/api/network/nat/create")
    public ResultUtil<NatModel> createNetworkComponentNat(@RequestParam("componentId") int componentId,
                                                          @RequestParam("localPort") int localPort,
                                                          @RequestParam("protocol") String protocol,
                                                          @RequestParam("remoteIp") String remoteIp,
                                                          @RequestParam("remotePort") int remotePort) {
        return this.lockRun(() -> networkService.createComponentNat(componentId, localPort, protocol, remoteIp, remotePort));
    }

    @DeleteMapping("/api/network/nat/destroy")
    public ResultUtil<Void> destroyNetworkComponentNat(@RequestParam("natId") int natId) {
        return this.lockRun(() -> networkService.deleteComponentNat(natId));
    }
}
