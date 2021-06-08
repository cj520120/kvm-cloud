package com.roamblue.cloud.management.service.impl;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import com.roamblue.cloud.management.data.entity.LoginInfoEntity;
import com.roamblue.cloud.management.data.mapper.LoginInfoMapper;
import com.roamblue.cloud.management.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleServiceImpl implements RuleService {
    @Autowired
    private LoginInfoMapper loginInfoMapper;

    @Override
    public void verifyPermission(int userId, int rule) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "用户不存在");
        }
        if (loginInfoEntity.getRuleType() > rule) {
            throw new CodeException(ErrorCode.PERMISSION_ERROR, "当前账号权限不足，请联系管理员进行操作");
        }
    }
}
