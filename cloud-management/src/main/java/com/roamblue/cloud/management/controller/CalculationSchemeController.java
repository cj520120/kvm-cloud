package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.CalculationSchemeInfo;
import com.roamblue.cloud.management.ui.SchemeUiService;
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
@Api(tags = "计算方案管理")
@Slf4j
public class CalculationSchemeController {
    @Autowired
    private SchemeUiService schemeUiService;

    @Login
    @GetMapping("/management/calculation/scheme")
    @ApiOperation(value = "获取计算方案列表")
    public ResultUtil<List<CalculationSchemeInfo>> listScheme() {
        return schemeUiService.listScheme();
    }

    @Login
    @GetMapping("/management/calculation/scheme/info")
    @ApiOperation(value = "根据ID查找计算方案")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID")
    })
    public ResultUtil<CalculationSchemeInfo> findSchemeById(@RequestParam("id") int id) {
        return schemeUiService.findSchemeById(id);
    }

    @Login
    @PostMapping("/management/calculation/scheme/create")
    @ApiOperation(value = "创建计算方案")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "名称"),
            @ApiImplicitParam(name = "cpu", value = "内核"),
            @ApiImplicitParam(name = "speed", value = "频率"),
            @ApiImplicitParam(name = "memory", value = "内存"),
            @ApiImplicitParam(name = "diskDriver", value = "磁盘驱动"),
            @ApiImplicitParam(name = "networkDriver", value = "网卡驱动")
    })
    public ResultUtil<CalculationSchemeInfo> createScheme(
            @RequestParam("name") String name,
            @RequestParam("cpu") int cpu,
            @RequestParam("speed") int speed,
            @RequestParam("memory") long memory
    ) {
        return schemeUiService.createScheme(name, cpu, speed, memory);
    }

    @Login
    @PostMapping("/management/calculation/scheme/destroy")
    @ApiOperation(value = "销毁计算方案")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "计算方案ID")
    })
    public ResultUtil<Void> destroy(@RequestParam("id") int id) {
        return schemeUiService.destroyScheme(id);
    }
}
