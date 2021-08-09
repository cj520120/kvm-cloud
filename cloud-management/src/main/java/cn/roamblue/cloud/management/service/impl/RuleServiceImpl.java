package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.RulePermissionInfo;
import cn.roamblue.cloud.management.data.entity.LoginInfoEntity;
import cn.roamblue.cloud.management.data.entity.RulePermissionEntity;
import cn.roamblue.cloud.management.data.mapper.LoginInfoMapper;
import cn.roamblue.cloud.management.data.mapper.RulePermissionMapper;
import cn.roamblue.cloud.management.service.RuleService;
import cn.roamblue.cloud.management.util.BeanConverter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenjun
 */
@Service
@Slf4j
public class RuleServiceImpl extends AbstractService implements RuleService {
    @Autowired
    private LoginInfoMapper loginInfoMapper;


    @Autowired
    private RulePermissionMapper rulePermissionMapper;

    @Autowired
    private RulePermissionMapper ruleGroupMapper;


    @Override
    public void hasPermission(int userId, String[] permissions) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "用户不存在");
        }

        RulePermissionEntity entity = rulePermissionMapper.selectById(loginInfoEntity.getRuleId());
        if(entity==null || StringUtils.isEmpty(entity.getGroupPermissions())){
            throw new CodeException(ErrorCode.PERMISSION_ERROR, localeMessage.getMessage("USER_PERMISSION_ERROR", "当前账号权限不足，请联系管理员进行操作"));
        }
        List<String> permissionList = Arrays.asList(entity.getGroupPermissions().split(","));
        for (String permission : permissions) {
            if (!permissionList.contains(permission)) {
                throw new CodeException(ErrorCode.PERMISSION_ERROR, localeMessage.getMessage("USER_PERMISSION_ERROR", "当前账号权限不足，请联系管理员进行操作"));
            }
        }

    }

    @Override
    public List<RulePermissionInfo> listRulePermission() {
        List<RulePermissionEntity> ruleGroupEntityList = ruleGroupMapper.selectList(new QueryWrapper<>());
        List<RulePermissionInfo> list = BeanConverter.convert(ruleGroupEntityList, this::init);
        return list;
    }

    @Override
    public RulePermissionInfo createRulePermission(String name, String[] permissions){
        RulePermissionEntity entity = RulePermissionEntity.builder()
                .groupName(name)
                .groupPermissions(String.join(",",permissions))
                .build();
        ruleGroupMapper.insert(entity);
        RulePermissionInfo clusterInfo = init(entity);
        log.info("create rule group={}", clusterInfo);
        return clusterInfo;
    }

    @Override
    public RulePermissionInfo modifyRulePermission(int id, String name, String[] permissions) {
        RulePermissionEntity entity = ruleGroupMapper.selectById(id);
        if (entity == null) {
            throw new CodeException(ErrorCode.RULE_PERMISSION_NOT_FOUND, localeMessage.getMessage("RULE_PERMISSION_NOT_FOUND", "权限组不存在"));
        }
        entity.setGroupName(name);
        entity.setGroupPermissions(String.join(",",permissions));
        ruleGroupMapper.updateById(entity);
        return this.init(entity);
    }

    @Override
    public void destroyRulePermissionById(int id) {
        log.info("destroy rule group id={}", id);
        ruleGroupMapper.deleteById(id);

    }

    private RulePermissionInfo init(RulePermissionEntity entity) {
        return RulePermissionInfo.builder().id(entity.getId()).name(entity.getGroupName()).permissions(Arrays.asList(entity.getGroupPermissions().split(","))).build();
    }
}
