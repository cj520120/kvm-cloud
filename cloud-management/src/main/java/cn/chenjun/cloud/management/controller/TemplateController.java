package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.model.TemplateModel;
import cn.chenjun.cloud.management.servcie.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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


        List<TemplateEntity> templateList = this.templateService.listTemplate();
        List<TemplateModel> models = templateList.stream().map(this.convertService::initTemplateModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @GetMapping("/api/template/search")
    public ResultUtil<Page<TemplateModel>> search(@RequestParam(value = "templateType", required = false) Integer templateType,
                                                  @RequestParam(value = "templateStatus", required = false) Integer templateStatus,
                                                  @RequestParam("keyword") String keyword,
                                                  @RequestParam("no") int no,
                                                  @RequestParam("size") int size) {
        Page<TemplateEntity> page = templateService.search(templateType, templateStatus, keyword, no, size);
        return ResultUtil.success(Page.convert(page, this.convertService::initTemplateModel));
    }

    @GetMapping("/api/template/info")
    public ResultUtil<TemplateModel> getTemplateInfo(@RequestParam("templateId") int templateId) {
        TemplateEntity template = templateService.getTemplateById(templateId);
        return ResultUtil.success(this.convertService.initTemplateModel(template));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/template/create")
    public ResultUtil<TemplateModel> createTemplate(@RequestParam("name") String name,
                                                    @RequestParam("uri") String uri,
                                                    @RequestParam("md5") String md5,
                                                    @RequestParam("templateType") int templateType,
                                                    @RequestParam("arch") String arch,
                                                    @RequestParam(value = "localCloudCfg", defaultValue = "") String localCloudCfg,
                                                    @RequestParam(value = "vendorData", defaultValue = "") String vendorData,
                                                    @RequestParam(value = "cloudWaitFlag", defaultValue = "0") int cloudWaitFlag) {
        TemplateEntity template = this.lockRun(() -> templateService.createTemplate(name, uri, md5, templateType, arch, localCloudCfg, vendorData, cloudWaitFlag));
        return ResultUtil.success(this.convertService.initTemplateModel(template));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/template/script")
    public ResultUtil<TemplateModel> updateTemplateScript(@RequestParam("templateId") int id,
                                                          @RequestParam(value = "localCloudCfg", defaultValue = "") String localCloudCfg,
                                                          @RequestParam(value = "vendorData", defaultValue = "") String vendorData,
                                                          @RequestParam(value = "cloudWaitFlag", defaultValue = "0") int cloudWaitFlag) {
        TemplateEntity template = this.lockRun(() -> templateService.updateTemplateScript(id, localCloudCfg, vendorData, cloudWaitFlag));
        return ResultUtil.success(this.convertService.initTemplateModel(template));
    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PostMapping("/api/template/download")
    public ResultUtil<TemplateModel> downloadTemplate(@RequestParam("templateId") int templateId) {
        TemplateEntity template = this.lockRun(() -> templateService.downloadTemplate(templateId));
        return ResultUtil.success(this.convertService.initTemplateModel(template));


    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.ADMIN)
    @PutMapping("/api/template/volume/create")
    public ResultUtil<TemplateModel> createVolumeTemplate(@RequestParam("volumeId") int volumeId,
                                                          @RequestParam("name") String name,
                                                          @RequestParam("arch") String arch) {
        TemplateEntity template = this.lockRun(() -> templateService.createVolumeTemplate(volumeId, name, arch));
        return ResultUtil.success(this.convertService.initTemplateModel(template));


    }

    @PermissionRequire(role = Constant.UserType.ADMIN)
    @DeleteMapping("/api/template/destroy")
    public ResultUtil<TemplateModel> destroyTemplate(@RequestParam("templateId") int templateId) {
        TemplateEntity template = this.lockRun(() -> templateService.destroyTemplate(templateId));
        return ResultUtil.success(this.convertService.initTemplateModel(template));
    }
}
