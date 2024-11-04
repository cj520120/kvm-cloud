package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.servcie.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author chenjun
 */
@LoginRequire
@RestController
public class TemplateController extends BaseController {
    @Autowired
    private TemplateService templateService;

    @GetMapping("/api/template/all")
    public ResultUtil<List<TemplateModel>> listTemplate() {
        return this.lockRun(() -> templateService.listTemplate());
    }

    @GetMapping("/api/template/info")
    public ResultUtil<TemplateModel> getTemplateInfo(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.getTemplateInfo(templateId));
    }

    @PutMapping("/api/template/create")
    public ResultUtil<TemplateModel> createTemplate(@RequestParam("name") String name,
                                                    @RequestParam("uri") String uri,
                                                    @RequestParam("md5") String md5,
                                                    @RequestParam("templateType") int templateType,
                                                    @RequestParam("volumeType") String volumeType,
                                                    @RequestParam(value = "script", defaultValue = "") String script) {
        return this.lockRun(() -> templateService.createTemplate(name, uri, md5, templateType, volumeType, script));
    }

    @PostMapping("/api/template/script")
    public ResultUtil<TemplateModel> updateTemplateScript(@RequestParam("templateId") int id,
                                                          @RequestParam(value = "script", defaultValue = "") String script) {
        return this.lockRun(() -> templateService.updateTemplateScript(id, script));
    }

    @PostMapping("/api/template/download")
    public ResultUtil<TemplateModel> downloadTemplate(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.downloadTemplate(templateId));

    }

    @PutMapping("/api/template/volume/create")
    public ResultUtil<TemplateModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("name") String name,
                                                          @RequestParam(value = "script", defaultValue = "") String script) {
        return this.lockRun(() -> templateService.createVolumeTemplate(volumeId, name, script));


    }

    @DeleteMapping("/api/template/destroy")
    public ResultUtil<TemplateModel> destroyTemplate(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.destroyTemplate(templateId));
    }
}
