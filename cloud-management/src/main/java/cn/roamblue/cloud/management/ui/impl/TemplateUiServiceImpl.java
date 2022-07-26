package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.PreAuthority;
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
    @PreAuthority(value = "hasAuthority('template.create')")
    public ResultUtil<TemplateInfo> createTemplate(int clusterId, int osCategoryId, String name, String type, String uri) {

        if (StringUtils.isEmpty(name)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "模版名称不能为空");
        }
        if (StringUtils.isEmpty(type)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "模版类型不能为空");
        }
        if (StringUtils.isEmpty(uri)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "模版下载地址不能为空");
        }
        if (osCategoryId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "模版系统类型不能为空");
        }

        if (clusterId <= 0) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "集群不能为空");
        }

        return super.call(() -> templateService.createTemplate(clusterId, osCategoryId, name, type, uri));
    }

    @Override
    @PreAuthority(value = "hasAuthority('template.destroy')")
    public ResultUtil<Void> destroyTemplate(int id) {
        return super.call(() -> templateService.destroyTemplateById(id));
    }

}
