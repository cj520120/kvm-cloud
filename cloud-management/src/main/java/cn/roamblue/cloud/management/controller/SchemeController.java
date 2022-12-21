package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.model.SchemeModel;
import cn.roamblue.cloud.management.servcie.SchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SchemeController {
    @Autowired
    private SchemeService schemeService;

    @GetMapping("/api/scheme/info")
    public ResultUtil<SchemeModel> getSchemeInfo(@RequestParam("schemeId") int schemeId) {
        return this.schemeService.getSchemeInfo(schemeId);
    }

    @GetMapping("/api/scheme/all")
    public ResultUtil<List<SchemeModel>> listScheme() {
        return this.schemeService.listScheme();
    }

    @PutMapping("/api/scheme/create")
    public ResultUtil<SchemeModel> createScheme(@RequestParam("name") String name,
                                                @RequestParam("cpu") int cpu,
                                                @RequestParam("memory") long memory,
                                                @RequestParam("speed") int speed,
                                                @RequestParam("sockets") int sockets,
                                                @RequestParam("cores") int cores,
                                                @RequestParam("threads") int threads) {
        return this.schemeService.createScheme(name, cpu, memory * 1024, speed, sockets, cores, threads);
    }


    @PostMapping("/api/scheme/modify")
    public ResultUtil<SchemeModel> updateScheme(@RequestParam("schemeId") int schemeId,
                                                @RequestParam("name") String name,
                                                @RequestParam("cpu") int cpu,
                                                @RequestParam("memory") long memory,
                                                @RequestParam("speed") int speed,
                                                @RequestParam("sockets") int sockets,
                                                @RequestParam("cores") int cores,
                                                @RequestParam("threads") int threads) {
        return this.schemeService.updateScheme(schemeId, name, cpu, memory * 1024, speed, sockets, cores, threads);
    }


    @DeleteMapping("/api/scheme/destroy")
    public ResultUtil<Void> destroyScheme(@RequestParam("schemeId") int schemeId) {
        return this.schemeService.destroyScheme(schemeId);
    }
}
