package cn.roamblue.cloud.management.ui;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface CategoryUiService {
    /**
     * 获取系统分类
     *
     * @return
     */
    ResultUtil<List<OsCategoryInfo>> listOsCategory();

    /**
     * 创建系统分类
     * @param categoryName
     * @param diskDriver
     * @param networkDriver
     * @return
     */
    ResultUtil<OsCategoryInfo> createCategory(String categoryName,String diskDriver,String networkDriver);

    /**
     * 修改系统分类
     * @param id
     * @param categoryName
     * @param diskDriver
     * @param networkDriver
     * @return
     */
    ResultUtil<OsCategoryInfo> modifyCategory(int id,String categoryName, String diskDriver, String networkDriver);

    /**
     * 删除系统分类
     * @param id
     * @return
     */
    ResultUtil<Void> destroyCategory(int id);

}
