package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.CalculationSchemeInfo;
import cn.roamblue.cloud.management.ui.SchemeUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 计算方案管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class CalculationSchemeController {
    @Autowired
    private SchemeUiService schemeUiService;

    /**
     * 获取计算方案列表
     *
     * @return
     */
    @Login
    @GetMapping("/management/calculation/scheme")
    public ResultUtil<List<CalculationSchemeInfo>> listScheme() {
        return schemeUiService.listScheme();
    }

    /**
     * 根据ID查找计算方案
     *
     * @param id
     * @return
     */
    @Login
    @GetMapping("/management/calculation/scheme/info")
    public ResultUtil<CalculationSchemeInfo> findSchemeById(@RequestParam("id") int id) {
        return schemeUiService.findSchemeById(id);
    }

    /**
     * 创建计算方案
     *
     * @param name   名称
     * @param cpu    内核
     * @param speed  频率
     * @param memory 内存
     * @return
     */
    @Login
    @PostMapping("/management/calculation/scheme/create")
    public ResultUtil<CalculationSchemeInfo> createScheme(
            @RequestParam("name") String name,
            @RequestParam("cpu") int cpu,
            @RequestParam("speed") int speed,
            @RequestParam("memory") long memory
    ) {
        return schemeUiService.createScheme(name, cpu, speed, memory);
    }

    /**
     * 销毁计算方案
     *
     * @param id 计算方案ID
     * @return
     */
    @Login
    @PostMapping("/management/calculation/scheme/destroy")
    public ResultUtil<Void> destroy(@RequestParam("id") int id) {
        return schemeUiService.destroyScheme(id);
    }
}
