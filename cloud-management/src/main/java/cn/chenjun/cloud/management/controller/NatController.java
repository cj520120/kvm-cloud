package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.NatModel;
import cn.chenjun.cloud.management.servcie.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class NatController extends BaseController {
    @Autowired
    private NetworkService networkService;

    @GetMapping("/api/nat/search")
    public ResultUtil<Page<NatModel>> search(@RequestParam("componentId") int componentId, @RequestParam(value = "keyword", required = false) String keyword,
                                             @RequestParam("no") int no,
                                             @RequestParam("size") int size) {
        return this.lockRun(() -> networkService.searchComponentNat(keyword, componentId, no, size));
    }

    @PutMapping("/api/nat/create")
    public ResultUtil<NatModel> createNetworkComponentNat(@RequestParam("componentId") int componentId,
                                                          @RequestParam("localPort") int localPort,
                                                          @RequestParam("protocol") String protocol,
                                                          @RequestParam("remoteIp") String remoteIp,
                                                          @RequestParam("remotePort") int remotePort) {
        return this.lockRun(() -> networkService.createComponentNat(componentId, localPort, protocol, remoteIp, remotePort));
    }

    @DeleteMapping("/api/nat/destroy")
    public ResultUtil<Void> destroyNetworkComponentNat(@RequestParam("natId") int natId) {
        return this.lockRun(() -> networkService.deleteComponentNat(natId));
    }
}
