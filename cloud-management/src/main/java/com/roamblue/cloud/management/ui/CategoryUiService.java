package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.OsCategoryInfo;

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
}
