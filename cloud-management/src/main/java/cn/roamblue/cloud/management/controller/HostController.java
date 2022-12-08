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
    public ResultUtil<HostModel> createHost(@RequestParam("name") String name,
                                            @RequestParam("ip") String ip,
                                            @RequestParam("uri") String uri,
                                            @RequestParam("nic") String nic) {
        return hostService.createHost(name, ip, uri, nic);
    }

    @PostMapping("/api/host/register")
    public ResultUtil<HostModel> registerHost(@RequestParam("hostId") int hostId) {
        return hostService.registerHost(hostId);
    }
}
