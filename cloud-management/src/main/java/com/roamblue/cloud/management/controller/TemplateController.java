package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.ui.TemplateUiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "模版管理")
@Slf4j
public class TemplateController {
    @Autowired
    private TemplateUiService templateUiService;

    @Login
    @GetMapping("/management/template")
    @ApiOperation(value = "获取模版列表")
    public ResultUtil<List<TemplateInfo>> listTemplates() {
        return templateUiService.listTemplates();
    }

    @Login
    @GetMapping("/management/template/search")
    @ApiOperation(value = "根据集群获取模版列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID")
    })
    public ResultUtil<List<TemplateInfo>> search(@RequestParam("clusterId") int clusterId) {
        return templateUiService.search(clusterId);
    }

    @Login
    @GetMapping("/management/template/info")
    @ApiOperation(value = "获取模版信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模版ID")
    })
    public ResultUtil<TemplateInfo> findTemplateById(@RequestParam("id") int id) {
        return templateUiService.findTemplateById(id);
    }

    @Login
    @PostMapping("/management/template/create")
    @ApiOperation(value = "创建模版")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "osCategoryId", value = "系统类型"),
            @ApiImplicitParam(name = "templateName", value = "模版名称"),
            @ApiImplicitParam(name = "templateType", value = "模版类型"),
            @ApiImplicitParam(name = "templateUri", value = "远程地址"),
    })
    public ResultUtil<TemplateInfo> createTemplate(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("osCategoryId") int osCategoryId,
            @RequestParam("templateName") String name,
            @RequestParam("templateType") String type,
            @RequestParam("templateUri") String uri) {

        return templateUiService.createTemplate(clusterId, osCategoryId, name, type, uri);
    }

    @Login
    @PostMapping("/management/template/destroy")
    @ApiOperation(value = "销毁模版")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模版ID")
    })
    public ResultUtil<Void> destroyTemplate(@RequestParam("id") int id) {
        return templateUiService.destroyTemplate(id);
    }

}
