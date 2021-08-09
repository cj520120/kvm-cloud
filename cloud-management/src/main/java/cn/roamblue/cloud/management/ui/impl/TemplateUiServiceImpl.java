package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.TemplateInfo;
import cn.roamblue.cloud.management.service.TemplateService;
import cn.roamblue.cloud.management.ui.TemplateUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenjun
 */
@Service
public class TemplateUiServiceImpl extends AbstractUiService implements TemplateUiService {
    @Autowired
    private TemplateService templateService;

    @Override
    public ResultUtil<List<TemplateInfo>> listTemplates() {
        return super.call(() -> templateService.listTemplates());
    }

    @Override
    public ResultUtil<List<TemplateInfo>> search(int clusterId) {
        return super.call(() -> templateService.search(clusterId));
    }

    @Override
    public ResultUtil<TemplateInfo> findTemplateById(int id) {
        return super.call(() -> templateService.findTemplateById(id));
    }

    @Override
    @Rule(permissions = "template.create")
    public ResultUtil<TemplateInfo> createTemplate(int clusterId, int osCategoryId, String name, String type, String uri) {

        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("TEMPLATE_NAME_EMPTY", "模版名称不能为空"));
        }
        if (StringUtils.isEmpty(type)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("TEMPLATE_TYPE_EMPTY", "模版类型不能为空"));
        }
        if (StringUtils.isEmpty(uri)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("TEMPLATE_URI_EMPTY", "模版下载地址不能为空"));
        }
        if (osCategoryId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("TEMPLATE_OS_CATEGORY_EMPTY", "模版系统类型不能为空"));
        }

        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, localeMessage.getMessage("MUST_HAS_CLUSTER", "集群不能为空"));
        }

        return super.call(() -> templateService.createTemplate(clusterId, osCategoryId, name, type, uri));
    }

    @Override
    @Rule(permissions = "template.destroy")
    public ResultUtil<Void> destroyTemplate(int id) {
        return super.call(() -> templateService.destroyTemplateById(id));
    }

}
