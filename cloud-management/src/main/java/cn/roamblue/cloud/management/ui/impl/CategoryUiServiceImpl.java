package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;
import cn.roamblue.cloud.management.service.OsCategoryService;
import cn.roamblue.cloud.management.ui.CategoryUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class CategoryUiServiceImpl extends AbstractUiService implements CategoryUiService {
    @Autowired
    private OsCategoryService osCategoryService;

    @Override
    public ResultUtil<List<OsCategoryInfo>> listOsCategory() {
        return super.call(() -> osCategoryService.listAllOsCategory());
    }
}
