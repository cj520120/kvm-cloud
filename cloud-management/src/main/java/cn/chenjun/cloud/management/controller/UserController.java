package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.annotation.NoLoginRequire;
import cn.chenjun.cloud.management.model.LoginSignatureModel;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.model.TokenModel;
import cn.chenjun.cloud.management.model.UserInfoModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.util.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理
 *
 * @author chenjun
 */
@LoginRequire
@RestController
@ResponseBody
public class UserController extends BaseController {
    @Autowired
    private UserService userUiService;

    @NoLoginRequire
    @PostMapping("/api/user/login")
    public ResultUtil<TokenModel> login(@RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("nonce") String nonce) {

        return this.lockRun(() -> userUiService.login(loginName, password, nonce));

    }

    @PostMapping("/api/user/password/modify")
    public ResultUtil<TokenModel> updatePassword(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("nonce") String nonce) {
        if (!Constant.UserType.LOCAL.equals(model.getType())) {
            return this.lockRun(() -> ResultUtil.error(ErrorCode.SERVER_ERROR, "Oauth2方式不支持修改密码"));
        }
        return this.lockRun(() -> userUiService.updatePassword(NumberUtil.parseInt(model.getId().toString()), oldPassword, newPassword, nonce));
    }

    @PostMapping("/api/user/token/refresh")
    public ResultUtil<TokenModel> refreshToken(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel loginUser) {
        return this.lockRun(() -> userUiService.refreshToken(loginUser));
    }

    @NoLoginRequire
    @GetMapping("/api/user/login/signature")
    public ResultUtil<LoginSignatureModel> getSignature(@RequestParam("loginName") String loginName) {
        return this.lockRun(() -> userUiService.getSignature(loginName));
    }

    @GetMapping("/api/user/signature")
    public ResultUtil<LoginSignatureModel> getLoginSignature(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model) {
        if (!Constant.UserType.LOCAL.equals(model.getType())) {
            return this.lockRun(() -> ResultUtil.error(ErrorCode.SERVER_ERROR, "Oauth2方式不支持获取签名"));
        }
        return this.lockRun(() -> userUiService.getLoginSignature(NumberUtil.parseInt(model.getId().toString())));
    }


    @PutMapping("/api/user/register")
    public ResultUtil<UserInfoModel> register(@RequestParam("loginName") String loginName, @RequestParam("password") String password) {

        return this.lockRun(() -> userUiService.register(loginName, password));

    }


    @PostMapping("/api/user/state/update")
    @LoginRequire
    public ResultUtil<UserInfoModel> updateUserState(@RequestParam("userId") int userId, @RequestParam("state") short state) {

        return this.lockRun(() -> userUiService.updateUserState(userId, state));
    }


    @DeleteMapping("/api/user/destroy")
    public ResultUtil<Void> destroyUser(@RequestParam("userId") int userId) {

        return this.lockRun(() -> userUiService.destroyUser(userId));
    }

    @PostMapping("/api/user/password/reset")
    @LoginRequire
    public ResultUtil<UserInfoModel> resetPassword(@RequestParam("userId") int userId, @RequestParam("password") String password) {
        return this.lockRun(() -> userUiService.resetPassword(userId, password));
    }

    @GetMapping("/api/user/list")
    public ResultUtil<List<UserInfoModel>> listUsers() {
        return this.lockRun(() -> userUiService.listUsers());
    }
}
