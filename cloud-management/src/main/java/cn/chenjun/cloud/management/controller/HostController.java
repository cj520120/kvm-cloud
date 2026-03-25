package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.model.HostModel;
import cn.chenjun.cloud.management.servcie.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        List<HostEntity> list = hostService.listAllHost();
        List<HostModel> modes = list.stream().map(this.convertService::initHostModel).collect(Collectors.toList());
        return ResultUtil.success(modes);
    }

    @GetMapping("/api/host/search")
    public ResultUtil<Page<HostModel>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam("no") int no,
                                              @RequestParam("size") int size) {
        Page<HostEntity> page = hostService.search(keyword, no, size);
        Page<HostModel> pageModel = Page.convert(page, this.convertService::initHostModel);
        return ResultUtil.success(pageModel);
    }

    @GetMapping("/api/host/info")
    public ResultUtil<HostModel> getHostInfo(@RequestParam("hostId") int hostId) {
        HostEntity entity = hostService.getHostInfo(hostId);
        return ResultUtil.success(convertService.initHostModel(entity));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/host/create")
    public ResultUtil<HostModel> createHost(@RequestParam("displayName") String displayName,
                                            @RequestParam("hostIp") String hostIp,
                                            @RequestParam("uri") String uri,
                                            @RequestParam("nic") String nic,
                                            @RequestParam("role") int role) {
        HostEntity entity = this.globalLockCall(() -> hostService.createHost(displayName, hostIp, uri, nic, role));
        return ResultUtil.success(convertService.initHostModel(entity));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/host/register")
    public ResultUtil<HostModel> registerHost(@RequestParam("hostId") int hostId) {
        HostEntity entity = this.globalLockCall(() -> hostService.registerHost(hostId));
        return ResultUtil.success(convertService.initHostModel(entity));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/host/maintenance")
    public ResultUtil<HostModel> maintenanceHost(@RequestParam("hostId") int hostId) {
        HostEntity entity = this.globalLockCall(() -> hostService.maintenanceHost(hostId));
        return ResultUtil.success(convertService.initHostModel(entity));
    }
    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/host/role/update")
    public ResultUtil<HostModel> updateHostRole(@RequestParam("hostId") int hostId,@RequestParam("role") int role) {
        HostEntity entity = this.globalLockCall(() -> hostService.updateHostRole(hostId, role));
        return ResultUtil.success(convertService.initHostModel(entity));
    }
    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/host/destroy")
    public ResultUtil<Void> destroyHost(@RequestParam("hostId") int hostId) {
        this.globalLockCall(() -> hostService.destroyHost(hostId));
        return ResultUtil.success();
    }
}
