package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.HostModel;
import cn.chenjun.cloud.management.servcie.HostService;
import cn.chenjun.cloud.management.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class HostController extends BaseController {
    @Autowired
    private HostService hostService;

    @GetMapping("/api/host/all")
    public ResultUtil<List<HostModel>> listAllHost() {
        return this.lockRun(() -> hostService.listAllHost());
    }

    @GetMapping("/api/host/search")
    public ResultUtil<Page<HostModel>> search(@RequestParam(value = "keyword",required = false) String keyword,
                                              @RequestParam("no") int no,
                                              @RequestParam("size") int size) {
        return this.lockRun(() -> hostService.search(keyword, no, size));
    }
    @GetMapping("/api/host/info")
    public ResultUtil<HostModel> getHostInfo(@RequestParam("hostId") int hostId) {
        return this.lockRun(() -> hostService.getHostInfo(hostId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PutMapping("/api/host/create")
    public ResultUtil<HostModel> createHost(@RequestParam("displayName") String displayName,
                                            @RequestParam("hostIp") String hostIp,
                                            @RequestParam("uri") String uri,
                                            @RequestParam("nic") String nic) {
        return this.lockRun(() -> hostService.createHost(displayName, hostIp, uri, nic));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PostMapping("/api/host/register")
    public ResultUtil<HostModel> registerHost(@RequestParam("hostId") int hostId) {
        return this.lockRun(() -> hostService.registerHost(hostId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @PostMapping("/api/host/maintenance")
    public ResultUtil<HostModel> maintenanceHost(@RequestParam("hostId") int hostId) {
        return this.lockRun(() -> hostService.maintenanceHost(hostId));
    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/host/destroy")
    public ResultUtil<Void> destroyHost(@RequestParam("hostId") int hostId) {
        return this.lockRun(() -> hostService.destroyHost(hostId));
    }
}
