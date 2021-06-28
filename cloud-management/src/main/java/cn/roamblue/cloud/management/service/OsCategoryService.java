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
}
