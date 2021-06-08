package com.roamblue.cloud.management.ui.impl;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.OsCategoryInfo;
import com.roamblue.cloud.management.service.OsCategoryService;
import com.roamblue.cloud.management.ui.CategoryUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryUiServiceImpl extends AbstractUiService implements CategoryUiService {
    @Autowired
    private OsCategoryService osCategoryService;

    @Override
    public ResultUtil<List<OsCategoryInfo>> listOsCategory() {
        return super.call(() -> osCategoryService.listAllOsCategory());
    }
}
