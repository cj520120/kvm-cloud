package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;
import cn.roamblue.cloud.management.ui.CategoryUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统类型
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class OsCategoryController {
    @Autowired
    private CategoryUiService categoryUiService;

    /**
     * 获取系统类型
     *
     * @return
     */
    @Login
    @GetMapping("/management/os/category")
    public ResultUtil<List<OsCategoryInfo>> listOsCategory() {
        return categoryUiService.listOsCategory();
    }
}
