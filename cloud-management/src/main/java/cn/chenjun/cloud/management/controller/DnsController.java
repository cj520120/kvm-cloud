package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.management.data.entity.DnsEntity;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.servcie.DnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

        List<DnsEntity> list = this.dnsService.listDnsByNetworkId(networkId);
        List<DnsModel> modelList = list.stream().map(this.convertService::initDnsModel).collect(Collectors.toList());
        return ResultUtil.success(modelList);

    }

    @LoginRequire
    @GetMapping("/api/dns/search")
    public ResultUtil<Page<DnsModel>> search(@RequestParam("networkId") int networkId,
                                             @RequestParam(value = "keyword", required = false) String keyword,
                                             @RequestParam("no") int no,
                                             @RequestParam("size") int size) {
        Page<DnsEntity> page = this.dnsService.search(networkId, keyword, no, size);
        Page<DnsModel> pageModel = Page.convert(page, this.convertService::initDnsModel);
        return ResultUtil.success(pageModel);
    }


    @LoginRequire
    @PutMapping("/api/dns/create")
    public ResultUtil<DnsModel> createDns(@RequestParam("networkId") int networkId,
                                          @RequestParam("domain") String domain,
                                          @RequestParam("ip") String ip) {
        DnsEntity entity = this.lockRun(() -> this.dnsService.createDns(networkId, domain, ip));
        DnsModel model = this.convertService.initDnsModel(entity);
        return ResultUtil.success(model);
    }


    @LoginRequire
    @DeleteMapping("/api/dns/destroy")
    public ResultUtil<Void> destroyDns(@RequestParam("id") int dnsId) {
        this.lockRun(() -> {
            this.dnsService.deleteDns(dnsId);
        });
        return ResultUtil.success();
    }


}
