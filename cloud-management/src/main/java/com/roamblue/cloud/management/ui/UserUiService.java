package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.LoginSignatureInfo;
import com.roamblue.cloud.management.bean.LoginUserTokenInfo;
import com.roamblue.cloud.management.bean.UserInfo;

import java.util.List;

public interface UserUiService {
    ResultUtil<LoginUserTokenInfo> login(String loginName, String password, String nonce);

    ResultUtil<LoginUserTokenInfo> updatePassword(Integer userId, String oldPassword, String newPassword, String nonce);

    ResultUtil<LoginUserTokenInfo> refreshToken(Integer userId);

    ResultUtil<LoginSignatureInfo> getSignature(String loginName);

    ResultUtil<LoginSignatureInfo> getLoginSignature(Integer userId);

    ResultUtil<UserInfo> register(String loginName, String password, int rule);

    ResultUtil<UserInfo> updateUserState(int currentUserId, int userId, short state);

    ResultUtil<UserInfo> updateUserRule(int currentUserId, int userId, int rule);

    ResultUtil<Void> destroyUser(int currentUserId, int userId);

    ResultUtil<UserInfo> resetPassword(int currentUserId, int userId, String password);

    ResultUtil<List<UserInfo>> listUsers();
}
