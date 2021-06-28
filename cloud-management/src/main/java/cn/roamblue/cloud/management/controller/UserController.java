package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.LoginSignatureInfo;
import cn.roamblue.cloud.management.bean.LoginUserTokenInfo;
import cn.roamblue.cloud.management.bean.UserInfo;
import cn.roamblue.cloud.management.ui.UserUiService;
import cn.roamblue.cloud.management.util.HttpHeaderNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理
 *
 * @author chenjun
 */
@RestController
@ResponseBody
public class UserController {
    @Autowired
    private UserUiService userUiService;

    /**
     * 登陆
     *
     * @param loginName 用户名
     * @param password  密码
     * @param nonce
     * @return
     */
    @PostMapping("/management/login")
    public ResultUtil<LoginUserTokenInfo> login(@RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("nonce") String nonce) {

        return userUiService.login(loginName, password, nonce);

    }

    /**
     * 修改密码
     *
     * @param userId
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param nonce
     * @return
     */
    @PostMapping("/management/password")
    @Login
    public ResultUtil<LoginUserTokenInfo> updatePassword(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("nonce") String nonce) {

        return userUiService.updatePassword(userId, oldPassword, newPassword, nonce);
    }

    /**
     * 刷新token
     *
     * @param userId
     * @return
     */
    @PostMapping("/management/token/refresh")
    @Login
    public ResultUtil<LoginUserTokenInfo> refreshToken(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId) {
        return userUiService.refreshToken(userId);
    }

    /**
     * 获取登陆签名
     *
     * @param loginName
     * @return
     */
    @GetMapping("/management/login/signature")
    public ResultUtil<LoginSignatureInfo> getSignature(@RequestParam("loginName") String loginName) {
        return userUiService.getSignature(loginName);
    }

    /**
     * 获取用户签名
     *
     * @param userId
     * @return
     */
    @GetMapping("/management/signature")
    @Login
    public ResultUtil<LoginSignatureInfo> getLoginSignature(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId) {
        return userUiService.getLoginSignature(userId);
    }

    /**
     * 注册用户
     *
     * @param loginName 用户名
     * @param password  密码
     * @param rule      权限
     * @return
     */
    @PostMapping("/management/user/register")
    @Login
    public ResultUtil<UserInfo> register(@RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("rule") int rule) {

        return userUiService.register(loginName, password, rule);

    }

    /**
     * 更新用户状态
     *
     * @param currentUserId
     * @param userId        用户名Id
     * @param state         用户状态
     * @return
     */
    @PostMapping("/management/user/state")
    @Login
    public ResultUtil<UserInfo> updateUserState(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("state") short state) {

        return userUiService.updateUserState(currentUserId, userId, state);
    }

    /**
     * 更新用户权限
     *
     * @param currentUserId
     * @param userId        用户名Id
     * @param rule          权限
     * @return
     */
    @PostMapping("/management/user/rule")
    @Login
    public ResultUtil<UserInfo> updateUserRule(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("rule") int rule) {

        return userUiService.updateUserRule(currentUserId, userId, rule);
    }

    /**
     * 更新用户权限
     *
     * @param currentUserId
     * @param userId        userId
     * @return
     */
    @PostMapping("/management/user/destroy")
    @Login
    public ResultUtil<Void> destroyUser(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId) {

        return userUiService.destroyUser(currentUserId, userId);
    }

    /**
     * 重置用户密码
     *
     * @param currentUserId
     * @param userId        用户名Id
     * @param password      密码
     * @return
     */
    @PostMapping("/management/user/reset/password")
    @Login
    public ResultUtil<UserInfo> resetPassword(@RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("password") String password) {
        return userUiService.resetPassword(currentUserId, userId, password);
    }

    /**
     * 获取用户列表
     *
     * @return
     */
    @GetMapping("/management/user/list")
    @Login
    public ResultUtil<List<UserInfo>> listUsers() {
        return userUiService.listUsers();
    }
}
