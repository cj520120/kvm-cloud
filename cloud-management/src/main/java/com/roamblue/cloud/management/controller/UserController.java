package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.LoginSignatureInfo;
import com.roamblue.cloud.management.bean.LoginUserTokenInfo;
import com.roamblue.cloud.management.bean.UserInfo;
import com.roamblue.cloud.management.ui.UserUiService;
import com.roamblue.cloud.management.util.HttpHeaderNames;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * @author chenjun
 */
@RestController
@ResponseBody
@Api(tags = "用户管理")
public class UserController {
    @Autowired
    private UserUiService userUiService;

    @PostMapping("/management/login")
    @ApiOperation(value = "登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginName", value = "用户名", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    public ResultUtil<LoginUserTokenInfo> login(@RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("nonce") String nonce) {

        return userUiService.login(loginName, password, nonce);

    }

    @PostMapping("/management/password")
    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPassword", value = "旧密码", required = true),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true)
    })
    @Login
    public ResultUtil<LoginUserTokenInfo> updatePassword(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("nonce") String nonce) {

        return userUiService.updatePassword(userId, oldPassword, newPassword, nonce);
    }

    @PostMapping("/management/token/refresh")
    @ApiOperation(value = "刷新token")
    @Login
    public ResultUtil<LoginUserTokenInfo> refreshToken(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId) {
        return userUiService.refreshToken(userId);
    }

    @GetMapping("/management/login/signature")
    @ApiOperation("/获取登陆签名")
    public ResultUtil<LoginSignatureInfo> getSignature(@RequestParam("loginName") String loginName) {
        return userUiService.getSignature(loginName);
    }

    @GetMapping("/management/signature")
    @ApiOperation("/获取用户签名")
    @Login
    public ResultUtil<LoginSignatureInfo> getLoginSignature(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) Integer userId) {
        return userUiService.getLoginSignature(userId);
    }

    @PostMapping("/management/user/register")
    @ApiOperation("注册用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginName", value = "用户名", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "rule", value = "权限", required = true)
    })
    @Login
    public ResultUtil<UserInfo> register(@RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("rule") int rule) {

        return userUiService.register(loginName, password, rule);

    }

    @PostMapping("/management/user/state")
    @ApiOperation("更新用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户名Id", required = true),
            @ApiImplicitParam(name = "state", value = "用户状态", required = true)
    })
    @Login
    public ResultUtil<UserInfo> updateUserState(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("state") short state) {

        return userUiService.updateUserState(currentUserId, userId, state);
    }

    @PostMapping("/management/user/rule")
    @ApiOperation("更新用户权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户名Id", required = true),
            @ApiImplicitParam(name = "rule", value = "权限", required = true)
    })
    @Login
    public ResultUtil<UserInfo> updateUserRule(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("rule") int rule) {

        return userUiService.updateUserRule(currentUserId, userId, rule);
    }

    @PostMapping("/management/user/destroy")
    @ApiOperation("更新用户权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户名Id", required = true)
    })
    @Login
    public ResultUtil<Void> destroyUser(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId) {

        return userUiService.destroyUser(currentUserId, userId);
    }


    @PostMapping("/management/user/reset/password")
    @ApiOperation("重置用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户名Id", required = true),
            @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    @Login
    public ResultUtil<UserInfo> resetPassword(@ApiIgnore @RequestAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE) int currentUserId, @RequestParam("userId") int userId, @RequestParam("password") String password) {
        return userUiService.resetPassword(currentUserId, userId, password);
    }

    @GetMapping("/management/user/list")
    @ApiOperation("获取用户列表")
    @Login
    public ResultUtil<List<UserInfo>> listUsers() {
        return userUiService.listUsers();
    }
}
