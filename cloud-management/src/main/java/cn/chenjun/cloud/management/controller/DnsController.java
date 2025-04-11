package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
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
public class DnsController extends BaseController {
    @Autowired
    private DnsService dnsService;


    @LoginRequire
    @GetMapping("/api/dns/list")
    public ResultUtil<List<DnsModel>> listDnsByNetworkId(@RequestParam("networkId") int networkId) {

        return this.lockRun(() -> this.dnsService.listDnsByNetworkId(networkId));

    }

    @LoginRequire
    @GetMapping("/api/dns/search")
    public ResultUtil<Page<DnsModel>> search(@RequestParam("networkId") int networkId,
                                             @RequestParam(value = "keyword",required = false) String keyword,
                                             @RequestParam("no") int no,
                                             @RequestParam("size") int size) {
        return this.lockRun(() -> this.dnsService.search(networkId, keyword, no, size));
    }

    @LoginRequire
    @PutMapping("/api/dns/create")
    public ResultUtil<DnsModel> createDns(@RequestParam("networkId") int networkId,
                                          @RequestParam("domain") String domain,
                                          @RequestParam("ip") String ip) {
        return this.lockRun(() -> this.dnsService.createDns(networkId, domain, ip));
    }

    @LoginRequire
    @DeleteMapping("/api/dns/destroy")
    public ResultUtil<Void> destroyDns(@RequestParam("dnsId") int dnsId) {
        return this.lockRun(() -> this.dnsService.deleteDns(dnsId));
    }


}
