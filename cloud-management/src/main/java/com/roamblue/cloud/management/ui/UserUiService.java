package com.roamblue.cloud.management.ui;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.bean.LoginSignatureInfo;
import com.roamblue.cloud.management.bean.LoginUserTokenInfo;
import com.roamblue.cloud.management.bean.UserInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface UserUiService {
    /**
     * 用户登陆
     *
     * @param loginName
     * @param password
     * @param nonce
     * @return
     */
    ResultUtil<LoginUserTokenInfo> login(String loginName, String password, String nonce);

    /**
     * 修改密码
     *
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @param nonce
     * @return
     */
    ResultUtil<LoginUserTokenInfo> updatePassword(Integer userId, String oldPassword, String newPassword, String nonce);

    /**
     * 刷新token
     *
     * @param userId
     * @return
     */
    ResultUtil<LoginUserTokenInfo> refreshToken(Integer userId);

    /**
     * 获取签名
     *
     * @param loginName
     * @return
     */
    ResultUtil<LoginSignatureInfo> getSignature(String loginName);

    /**
     * 获取登陆用户签名
     *
     * @param userId
     * @return
     */
    ResultUtil<LoginSignatureInfo> getLoginSignature(Integer userId);

    /**
     * 用户注册
     *
     * @param loginName
     * @param password
     * @param rule
     * @return
     */
    ResultUtil<UserInfo> register(String loginName, String password, int rule);

    /**
     * 更新用户状态
     *
     * @param currentUserId
     * @param userId
     * @param state
     * @return
     */
    ResultUtil<UserInfo> updateUserState(int currentUserId, int userId, short state);

    /**
     * 更新用户权限
     *
     * @param currentUserId
     * @param userId
     * @param rule
     * @return
     */
    ResultUtil<UserInfo> updateUserRule(int currentUserId, int userId, int rule);

    /**
     * 删除用户
     *
     * @param currentUserId
     * @param userId
     * @return
     */
    ResultUtil<Void> destroyUser(int currentUserId, int userId);

    /**
     * 管理员重置密码
     *
     * @param currentUserId
     * @param userId
     * @param password
     * @return
     */
    ResultUtil<UserInfo> resetPassword(int currentUserId, int userId, String password);

    /**
     * 获取用户列表
     *
     * @return
     */
    ResultUtil<List<UserInfo>> listUsers();
}
