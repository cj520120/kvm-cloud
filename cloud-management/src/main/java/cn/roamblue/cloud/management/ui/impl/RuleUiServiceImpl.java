package cn.roamblue.cloud.management.ui.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.bean.RulePermissionInfo;
import cn.roamblue.cloud.management.service.RuleService;
import cn.roamblue.cloud.management.ui.RuleUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: RuleUiServiceImpl
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/9 下午6:02
 */
@Service
public class RuleUiServiceImpl extends AbstractUiService implements RuleUiService {
    @Autowired
    private RuleService ruleService;
    @Override
    public ResultUtil<List<RulePermissionInfo>> listRulePermission() {
        return super.call(() -> ruleService.listRulePermission());
    }

    @Override
    @Rule(permissions = "rule.permission.create")
    public ResultUtil<RulePermissionInfo> createRulePermission(String name, String[] permissions) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, localeMessage.getMessage("RULE_PERMISSION_NAME_EMPTY", "名称不能为空"));
        }
        return super.call(() -> ruleService.createRulePermission(name,permissions));
    }

    @Rule(permissions = "rule.permission.modify")
    @Override
    public ResultUtil<RulePermissionInfo> modifyRulePermission(int id, String name, String[] permissions) {
        if (StringUtils.isEmpty(name)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, localeMessage.getMessage("RULE_PERMISSION_NAME_EMPTY", "名称不能为空"));
        }
        return super.call(() -> ruleService.modifyRulePermission(id,name,permissions));
    }

    @Rule(permissions = "rule.permission.destroy")
    @Override
    public ResultUtil<Void> destroyRulePermissionById(int id) {
        return super.call(() -> ruleService.destroyRulePermissionById(id));
    }
}
