package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.OsCategoryInfo;

import java.util.List;

public interface CategoryUiService {
    ResultUtil<List<OsCategoryInfo>> listOsCategory();
}
