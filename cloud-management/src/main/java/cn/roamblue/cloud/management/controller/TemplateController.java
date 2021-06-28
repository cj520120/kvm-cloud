package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.TemplateInfo;
import cn.roamblue.cloud.management.ui.TemplateUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模版管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class TemplateController {
    @Autowired
    private TemplateUiService templateUiService;

    /**
     * 获取模版列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/template")
    public ResultUtil<List<TemplateInfo>> listTemplates() {
        return templateUiService.listTemplates();
    }

    /**
     * 根据集群获取模版列表
     *
     * @param clusterId
     * @return
     */
    @Login
    @GetMapping("/management/template/search")
    public ResultUtil<List<TemplateInfo>> search(@RequestParam("clusterId") int clusterId) {
        return templateUiService.search(clusterId);
    }

    /**
     * 获取模版信息
     *
     * @param id
     * @return
     */
    @Login
    @GetMapping("/management/template/info")
    public ResultUtil<TemplateInfo> findTemplateById(@RequestParam("id") int id) {
        return templateUiService.findTemplateById(id);
    }

    /**
     * 创建模版
     *
     * @param clusterId    集群ID
     * @param osCategoryId 系统类型
     * @param name         模版名称
     * @param type         模版类型
     * @param uri          远程地址
     * @return
     */
    @Login
    @PostMapping("/management/template/create")
    public ResultUtil<TemplateInfo> createTemplate(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("osCategoryId") int osCategoryId,
            @RequestParam("templateName") String name,
            @RequestParam("templateType") String type,
            @RequestParam("templateUri") String uri) {

        return templateUiService.createTemplate(clusterId, osCategoryId, name, type, uri);
    }

    /**
     * 销毁模版
     *
     * @param id
     * @return
     */
    @Login
    @PostMapping("/management/template/destroy")
    public ResultUtil<Void> destroyTemplate(@RequestParam("id") int id) {
        return templateUiService.destroyTemplate(id);
    }

}
