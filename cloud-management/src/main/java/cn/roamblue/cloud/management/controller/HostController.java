package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.model.HostModel;
import cn.roamblue.cloud.management.servcie.HostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HostController {
    @Autowired
    private HostService hostService;

    @GetMapping("/api/host/all")
    public ResultUtil<List<HostModel>> listAllHost() {
        return hostService.listAllHost();
    }

    @GetMapping("/api/host/info")
    public ResultUtil<HostModel> getHostInfo(@RequestParam("hostId") int hostId) {
        return hostService.getHostInfo(hostId);
    }

    @PutMapping("/api/host/create")
    public ResultUtil<HostModel> createHost(@RequestParam("displayName") String displayName,
                                            @RequestParam("hostIp") String hostIp,
                                            @RequestParam("uri") String uri,
                                            @RequestParam("nic") String nic) {
        return hostService.createHost(displayName, hostIp, uri, nic);
    }

    @PostMapping("/api/host/register")
    public ResultUtil<HostModel> registerHost(@RequestParam("hostId") int hostId) {
        return hostService.registerHost(hostId);
    }

    @PostMapping("/api/host/maintenance")
    public ResultUtil<HostModel> maintenanceHost(@RequestParam("hostId") int hostId) {
        return hostService.maintenanceHost(hostId);
    }

    @DeleteMapping("/api/host/destroy")
    public ResultUtil<Void> destroyHost(@RequestParam("hostId") int hostId) {
        return hostService.destroyHost(hostId);
    }
}
