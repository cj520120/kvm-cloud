package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
public class DnsController {
    @Autowired
    private DnsService dnsService;


    @LoginRequire
    @GetMapping("/api/dns/list")
    public ResultUtil<List<DnsModel>> listDnsByNetworkId(@RequestParam("networkId") int networkId) {
        return this.dnsService.listDnsByNetworkId(networkId);
    }

    @LoginRequire
    @PutMapping("/api/dns/create")
    public ResultUtil<DnsModel> createDns(@RequestParam("networkId") int networkId,
                                          @RequestParam("domain") String domain,
                                          @RequestParam("ip") String ip) {
        return this.dnsService.createDns(networkId, domain, ip);
    }

    @LoginRequire
    @DeleteMapping("/api/dns/destroy")
    public ResultUtil<Void> destroyDns(@RequestParam("dnsId") int dnsId) {
        return this.dnsService.deleteDns(dnsId);
    }


}
