package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.RouteStrategyEntity;
import cn.chenjun.cloud.management.model.ComponentDetailModel;
import cn.chenjun.cloud.management.model.GuestModel;
import cn.chenjun.cloud.management.model.RouteStrategyModel;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.servcie.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@LoginRequire
@RestController
public class ComponentController extends BaseController {
    @Autowired
    private NetworkService networkService;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private GuestService guestService;

    @GetMapping("/api/component/all")
    public ResultUtil<List<ComponentDetailModel>> listNetworkComponent(@RequestParam("networkId") int networkId) {
        List<ComponentEntity> entityList = this.networkService.listNetworkComponent(networkId);
        List<ComponentDetailModel> list = new ArrayList<>();
        for (ComponentEntity componentEntity : entityList) {
            list.add(this.convertService.initComponentModel(componentEntity));
        }
        return ResultUtil.success(list);

    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/component/nat/create")
    public ResultUtil<ComponentDetailModel> createNatComponent(@RequestParam("networkId") int networkId) {
        ComponentEntity componentEntity = this.globalLockCall(() -> this.networkService.createComponent(networkId, cn.chenjun.cloud.common.util.Constant.ComponentType.NAT));
        return ResultUtil.success(this.convertService.initComponentModel(componentEntity));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/component")
    public ResultUtil<Void> destroyNetworkComponent(@RequestParam("componentId") int componentId) {
        this.globalLockCall(() -> networkService.destroyComponent(componentId));
        return ResultUtil.success();
    }

    @GetMapping("/api/component/guest")
    public ResultUtil<List<GuestModel>> listSystemGuests(@RequestParam("componentId") int componentId) {
        List<GuestEntity> guests = this.guestService.listSystemGuests(componentId);
        return ResultUtil.success(this.convertService.initGuestList(guests));
    }

    @LoginRequire
    @GetMapping("/api/component/route/search")
    public ResultUtil<Page<RouteStrategyModel>> search(@RequestParam("componentId") int componentId,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam("no") int no,
                                                       @RequestParam("size") int size) {
        Page<RouteStrategyEntity> page = this.componentService.searchRouteStrategy(componentId, keyword, no, size);
        Page<RouteStrategyModel> pageModel = Page.convert(page, this.convertService::initRouteStrategy);
        return ResultUtil.success(pageModel);
    }


    @GetMapping("/api/component/route")
    public ResultUtil<List<RouteStrategyModel>> listComponentRoutes(@RequestParam("componentId") int componentId) {
        List<RouteStrategyEntity> routes = this.componentService.listRouteStrategyByComponentId(componentId);
        return ResultUtil.success(this.convertService.initRouteStrategy(routes));
    }

    @PutMapping("/api/component/route")
    public ResultUtil<RouteStrategyModel> createComponentRoutes(@RequestParam("componentId") int componentId, @RequestParam("destIp") String destIp, @RequestParam("cidr") int cidr, @RequestParam("nexthop") String nexthop) {
        RouteStrategyEntity route = this.componentService.createRoute(componentId, destIp, cidr, nexthop);
        return ResultUtil.success(this.convertService.initRouteStrategy(route));
    }

    @DeleteMapping("/api/component/route")
    public ResultUtil<Void> deleteComponentRoute(@RequestParam("id") int id) {
        this.componentService.deleteRouteStrategyById(id);
        return ResultUtil.success();
    }

}
