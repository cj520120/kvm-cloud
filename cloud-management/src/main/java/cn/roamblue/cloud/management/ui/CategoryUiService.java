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
}
