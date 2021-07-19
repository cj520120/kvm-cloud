package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;
import cn.roamblue.cloud.management.ui.CategoryUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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




    /**
     * 创建系统类型
     *
     * @return
     */
    @Login
    @PostMapping("/management/os/category/create")
    public ResultUtil<OsCategoryInfo> createOsCategoryInfo(@RequestParam("categoryName") String categoryName,
                                                          @RequestParam("diskDriver") String diskDriver,
                                                          @RequestParam("networkDriver") String networkDriver) {
        return categoryUiService.createCategory(categoryName, diskDriver, networkDriver);
    }
    /**
     * 创建系统类型
     *
     * @return
     */
    @Login
    @PostMapping("/management/os/category/modify")
    public ResultUtil<OsCategoryInfo> modifyOsCategoryInfo(@RequestParam("id") int id,
                                                           @RequestParam("categoryName") String categoryName,
                                                          @RequestParam("diskDriver") String diskDriver,
                                                          @RequestParam("networkDriver") String networkDriver) {
        return categoryUiService.modifyCategory(id,categoryName, diskDriver, networkDriver);
    }

    /**
     * 销毁系统类型
     *
     * @return
     */
    @Login
    @PostMapping("/management/os/category/destroy")
    public ResultUtil<Void> destoryCategoryById(@RequestParam("id") int id) {
        return categoryUiService.destroyCategory(id);
    }
}
