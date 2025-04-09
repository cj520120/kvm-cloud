package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.SchemeModel;
import cn.chenjun.cloud.management.servcie.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class SchemeController extends BaseController {
    @Autowired
    private SchemeService schemeService;

    @GetMapping("/api/scheme/info")
    public ResultUtil<SchemeModel> getSchemeInfo(@RequestParam("schemeId") int schemeId) {
        return this.lockRun(() -> this.schemeService.getSchemeInfo(schemeId));
    }

    @GetMapping("/api/scheme/all")
    public ResultUtil<List<SchemeModel>> listScheme() {
        return this.lockRun(() -> this.schemeService.listScheme());
    }

    @GetMapping("/api/scheme/search")
    public ResultUtil<Page<SchemeModel>> search(@RequestParam("keyword") String keyword,
                                                @RequestParam("no") int no,
                                                @RequestParam("size") int size) {
        return this.lockRun(() -> this.schemeService.search(keyword, no, size));
    }
    @PutMapping("/api/scheme/create")
    public ResultUtil<SchemeModel> createScheme(@RequestParam("name") String name,
                                                @RequestParam("cpu") int cpu,
                                                @RequestParam("memory") long memory,
                                                @RequestParam("share") int share,
                                                @RequestParam("sockets") int sockets,
                                                @RequestParam("cores") int cores,
                                                @RequestParam("threads") int threads) {
        return this.lockRun(() -> this.schemeService.createScheme(name, cpu, memory * 1024, share, sockets, cores, threads));
    }


    @PostMapping("/api/scheme/modify")
    public ResultUtil<SchemeModel> updateScheme(@RequestParam("schemeId") int schemeId,
                                                @RequestParam("name") String name,
                                                @RequestParam("cpu") int cpu,
                                                @RequestParam("memory") long memory,
                                                @RequestParam("share") int share,
                                                @RequestParam("sockets") int sockets,
                                                @RequestParam("cores") int cores,
                                                @RequestParam("threads") int threads) {
        return this.lockRun(() -> this.schemeService.updateScheme(schemeId, name, cpu, memory * 1024, share, sockets, cores, threads));
    }


    @DeleteMapping("/api/scheme/destroy")
    public ResultUtil<Void> destroyScheme(@RequestParam("schemeId") int schemeId) {
        return this.lockRun(() -> this.schemeService.destroyScheme(schemeId));
    }
}
