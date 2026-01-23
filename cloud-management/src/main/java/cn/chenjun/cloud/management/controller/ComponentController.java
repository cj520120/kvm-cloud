package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.ComponentDetailModel;
import cn.chenjun.cloud.management.model.GuestModel;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@LoginRequire
@RestController
public class ComponentController extends BaseController {
    @Autowired
    private NetworkService networkService;
    @Autowired
    private GuestService guestService;

    @GetMapping("/api/component/all")
    public ResultUtil<List<ComponentDetailModel>> listNetworkComponent(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.listNetworkComponent(networkId));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/component/nat/create")
    public ResultUtil<ComponentDetailModel> createNatComponent(@RequestParam("networkId") int networkId) {
        return this.lockRun(() -> networkService.createComponent(networkId, cn.chenjun.cloud.common.util.Constant.ComponentType.NAT));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/component")
    public ResultUtil<Void> destroyNetworkComponent(@RequestParam("componentId") int componentId) {
        return this.lockRun(() -> networkService.destroyComponent(componentId));
    }

    @GetMapping("/api/component/guest")
    public ResultUtil<List<GuestModel>> listSystemGuests(@RequestParam("componentId") int componentId) {
        return this.lockRun(() -> this.guestService.listSystemGuests(componentId));
    }
}
