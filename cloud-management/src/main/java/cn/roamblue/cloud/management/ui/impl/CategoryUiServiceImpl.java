package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.OsCategoryInfo;
import cn.roamblue.cloud.management.service.OsCategoryService;
import cn.roamblue.cloud.management.ui.CategoryUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public ResultUtil<OsCategoryInfo> createCategory(String categoryName, String diskDriver, String networkDriver) {

        if (StringUtils.isEmpty(categoryName)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("CATEGORY_NAME_NOT_EMPTY", "名称不能为空"));
        }

        if (StringUtils.isEmpty(diskDriver)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("DISK_DRIVE_EMPTY", "硬盘驱动不能为空"));
        }

        if (StringUtils.isEmpty(networkDriver)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("NETWORK_DRIVE_EMPTY", "网络驱动不能为空"));
        }
        return super.call(() -> osCategoryService.createOsCategory(categoryName, diskDriver, networkDriver));
    }

    @Override
    public ResultUtil<OsCategoryInfo> modifyCategory(int id, String categoryName, String diskDriver, String networkDriver) {
        if (StringUtils.isEmpty(categoryName)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("CATEGORY_NAME_NOT_EMPTY", "名称不能为空"));
        }

        if (StringUtils.isEmpty(diskDriver)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("DISK_DRIVE_EMPTY", "硬盘驱动不能为空"));
        }

        if (StringUtils.isEmpty(networkDriver)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("NETWORK_DRIVE_EMPTY", "网络驱动不能为空"));
        }
        return super.call(() -> osCategoryService.modifyOsCategory(id, categoryName, diskDriver, networkDriver));
    }

    @Override
    public ResultUtil<Void> destroyCategory(int id) {
        return super.call(() -> osCategoryService.destroyOsCategoryById(id));
    }
}
