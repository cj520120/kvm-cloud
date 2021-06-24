package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.OsCategoryInfo;
import com.roamblue.cloud.management.ui.CategoryUiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
@Api(tags = "系统类型")
@Slf4j
public class OsCategoryController {
    @Autowired
    private CategoryUiService categoryUiService;

    @Login
    @GetMapping("/management/os/category")
    @ApiOperation(value = "获取系统类型")
    public ResultUtil<List<OsCategoryInfo>> listOsCategory() {
        return categoryUiService.listOsCategory();
    }
}
