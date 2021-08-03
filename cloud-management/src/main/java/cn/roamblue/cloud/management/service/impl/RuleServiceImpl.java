package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.LoginInfoEntity;
import cn.roamblue.cloud.management.data.mapper.LoginInfoMapper;
import cn.roamblue.cloud.management.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author chenjun
 */
@Service
public class RuleServiceImpl extends AbstractService implements RuleService {
    @Autowired
    private LoginInfoMapper loginInfoMapper;

    @Override
    public void hasPermission(int userId, int rule) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "用户不存在"));
        }
        if (loginInfoEntity.getRuleType() > rule) {
            throw new CodeException(ErrorCode.PERMISSION_ERROR, localeMessage.getMessage("USER_PERMISSION_ERROR", "当前账号权限不足，请联系管理员进行操作"));
        }
    }
}
