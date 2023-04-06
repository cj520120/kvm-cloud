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
public class TemplateController {
    @Autowired
    private TemplateService templateService;

    @GetMapping("/api/template/all")
    public ResultUtil<List<TemplateModel>> listTemplate() {
        return templateService.listTemplate();
    }

    @GetMapping("/api/template/info")
    public ResultUtil<TemplateModel> getTemplateInfo(@RequestParam("templateId") int templateId) {
        return templateService.getTemplateInfo(templateId);
    }

    @PutMapping("/api/template/create")
    public ResultUtil<TemplateModel> createTemplate(@RequestParam("name") String name,
                                                    @RequestParam("uri") String uri,
                                                    @RequestParam("templateType") int templateType,
                                                    @RequestParam("volumeType") String volumeType) {
        return templateService.createTemplate(name, uri, templateType, volumeType);
    }

    @PostMapping("/api/template/download")
    public ResultUtil<TemplateModel> downloadTemplate(@RequestParam("templateId") int templateId) {
        return templateService.downloadTemplate(templateId);

    }

    @PutMapping("/api/template/volume/create")
    public ResultUtil<TemplateModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("name") String name) {
        return templateService.createVolumeTemplate(volumeId, name);


    }

    @DeleteMapping("/api/template/destroy")
    public ResultUtil<TemplateModel> destroyTemplate(@RequestParam("templateId") int templateId) {
        return templateService.destroyTemplate(templateId);
    }
}
