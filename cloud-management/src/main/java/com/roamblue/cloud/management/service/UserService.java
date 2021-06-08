package com.roamblue.cloud.management.service;


import com.roamblue.cloud.management.bean.LoginUserInfo;
import com.roamblue.cloud.management.bean.LoginUserTokenInfo;
import com.roamblue.cloud.management.bean.UserInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface UserService {
    /**
     * 登陆
     *
     * @param loginName 用户名
     * @param password  密码
     * @param nonce     nonce
     * @return
     */
    LoginUserTokenInfo login(String loginName, String password, String nonce);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return
     */
    LoginUserInfo findUserById(int userId);

    /**
     * 根据登陆名获取用户信息
     *
     * @param loginName 登录名
     * @return
     */
    LoginUserInfo findUserByLoginName(String loginName);

    /**
     * 根据token获取用户ID
     *
     * @param token token
     * @return
     */
    Integer getUserIdByToken(String token);

    /**
     * 验证token合法性
     *
     * @param token token
     * @return
     */
    Integer verify(String token);

    /**
     * 更新密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param nonce       noce
     * @return
     */
    LoginUserTokenInfo updatePassword(Integer userId, String oldPassword, String newPassword, String nonce);

    /**
     * 刷新token
     *
     * @param userId
     * @return
     */
    LoginUserTokenInfo refreshToken(Integer userId);

    /**
     * 注册用户
     *
     * @param loginName
     * @param password
     * @return
     */
    UserInfo register(String loginName, String password, int rule);

    UserInfo updateUserState(int userId, short state);

    UserInfo updateUserRule(int userId, int rule);

    List<UserInfo> listUsers();

    UserInfo resetPassword(int userId, String password);

    void destroyUser(int userId);
}
