package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
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

    @GetMapping("/api/template/search")
    public ResultUtil<Page<TemplateModel>> search(@RequestParam(value = "templateType", required = false) Integer templateType,
                                                  @RequestParam(value = "templateStatus", required = false) Integer templateStatus,
                                                  @RequestParam("keyword") String keyword,
                                                  @RequestParam("no") int no,
                                                  @RequestParam("size") int size) {
        return this.lockRun(() -> templateService.search(templateType, templateStatus, keyword, no, size));
    }

    @GetMapping("/api/template/info")
    public ResultUtil<TemplateModel> getTemplateInfo(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.getTemplateInfo(templateId));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/template/create")
    public ResultUtil<TemplateModel> createTemplate(@RequestParam("name") String name,
                                                    @RequestParam("uri") String uri,
                                                    @RequestParam("md5") String md5,
                                                    @RequestParam("templateType") int templateType,
                                                    @RequestParam(value = "script", defaultValue = "") String script) {
        return this.lockRun(() -> templateService.createTemplate(name, uri, md5, templateType, script));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/template/script")
    public ResultUtil<TemplateModel> updateTemplateScript(@RequestParam("templateId") int id,
                                                          @RequestParam(value = "script", defaultValue = "") String script) {
        return this.lockRun(() -> templateService.updateTemplateScript(id, script));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/template/download")
    public ResultUtil<TemplateModel> downloadTemplate(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.downloadTemplate(templateId));

    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/template/volume/create")
    public ResultUtil<TemplateModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("name") String name) {
        return this.lockRun(() -> templateService.createVolumeTemplate(volumeId, name));


    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/template/destroy")
    public ResultUtil<TemplateModel> destroyTemplate(@RequestParam("templateId") int templateId) {
        return this.lockRun(() -> templateService.destroyTemplate(templateId));
    }
}
