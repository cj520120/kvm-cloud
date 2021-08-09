package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.*;
import cn.roamblue.cloud.management.ui.RuleUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 虚拟管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class RuleController {
    @Autowired
    private RuleUiService ruleUiService;

    /**
     * 获取所有权限组
     *
     * @return
     */
    @Login
    @GetMapping("/management/rules")
    public ResultUtil<List<RulePermissionInfo>> listAllVm() {
        return ruleUiService.listRulePermission();
    }


    /**
     * 添加权限组
     *
     * @param name              权限组名称
     * @param permissions       具体权限，逗号分割
     * @return
     */
    @Login
    @PostMapping("/management/rules/create")
    public ResultUtil<RulePermissionInfo> createRule(
            @RequestParam("name") String name,
            @RequestParam("permissions") String permissions) {

        return ruleUiService.createRulePermission(name,permissions.split(","));
    }

    /**
     * 更新权限组
     *
     * @param id                权限组ID
     * @param name              权限组名称
     * @param permissions       具体权限，逗号分割
     * @return
     */
    @Login
    @PostMapping("/management/rules/modify")
    public ResultUtil<RulePermissionInfo> modifyRulePermission(
            @RequestParam("id") int id,
            @RequestParam("name") String name,
            @RequestParam("permissions") String permissions) {

        return ruleUiService.modifyRulePermission(id,name,permissions.split(","));
    }
    /**
     * 删除权限组
     *
     * @param id       权限组ID
     * @return
     */
    @Login
    @PostMapping("/management/rules/destroy")
    public ResultUtil<Void> destroyRulePermissionById(@RequestParam("id") int id) {
        return ruleUiService.destroyRulePermissionById(id);
    }
}
