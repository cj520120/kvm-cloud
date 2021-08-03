package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.OsCategoryInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface OsCategoryService {
    /**
     * 根据ID获取系统分类
     *
     * @param id
     * @return
     */
    OsCategoryInfo findOsCategoryById(int id);

    /**
     * 获取系统分类
     *
     * @return
     */
    List<OsCategoryInfo> listAllOsCategory();

    /**
     * 创建系统分类
     *
     * @param categoryName
     * @param networkDriver
     * @param diskDriver
     * @return
     */
    OsCategoryInfo createOsCategory(String categoryName, String networkDriver, String diskDriver);

    /**
     * 修改系统分类
     *
     * @param id
     * @param categoryName
     * @param diskDriver
     * @param networkDriver
     * @return
     */
    OsCategoryInfo modifyOsCategory(int id, String categoryName, String diskDriver, String networkDriver);

    /***
     * 删除系统分类
     * @param id
     */
    void destroyOsCategoryById(int id);
}
