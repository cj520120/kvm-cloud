package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.NoLoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.util.Constant;
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

    @PostMapping("/api/user/self/modify")
    public ResultUtil<Void> updateSelfInfo(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model, @RequestParam(value = "username", defaultValue = "") String username, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("nonce") String nonce) {

        return this.lockRun(() -> userUiService.updateSelfInfo(model.getUserId(), username, oldPassword, newPassword, nonce));
    }

    @PostMapping("/api/user/token/refresh")
    public ResultUtil<RefreshTokenModel> refreshToken(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel loginUser) {
        return this.lockRun(() -> userUiService.refreshToken(loginUser.getUserId()));
    }

    @NoLoginRequire
    @GetMapping("/api/user/login/signature")
    public ResultUtil<LoginSignatureModel> getSignature(@RequestParam("loginName") String loginName) {
        return this.lockRun(() -> userUiService.getSignature(loginName));
    }

    @GetMapping("/api/user/signature")
    public ResultUtil<LoginSignatureModel> getLoginSignature(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model) {

        return this.lockRun(() -> userUiService.getLoginSignature(model.getUserId()));
    }


    @PermissionRequire(role = Constant.UserType.SUPPER_ADMIN)
    @PutMapping("/api/user/register")
    public ResultUtil<UserInfoModel> register(@RequestParam("userName") String userName, @RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("userType") short userType, @RequestParam("userStatus") short userStatus) {

        return this.lockRun(() -> userUiService.register(userName, loginName, password, userType, userStatus));

    }

    @GetMapping("/api/user/self")
    public ResultUtil<UserInfoModel> getSelfInfo(@RequestAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model) {

        return this.lockRun(() -> userUiService.getUserInfo(model.getUserId()));

    }

    @PermissionRequire(role = Constant.UserType.SUPPER_ADMIN)
    @PostMapping("/api/user/update")
    @LoginRequire
    public ResultUtil<UserInfoModel> updateUserInfo(@RequestParam("userId") int userId, @RequestParam("userName") String userName, @RequestParam("userType") short userType, @RequestParam("userStatus") short userStatus) {
        return this.lockRun(() -> userUiService.updateUser(userId, userName, userType, userStatus));
    }


    @PermissionRequire(role = Constant.UserType.SUPPER_ADMIN)
    @DeleteMapping("/api/user/destroy")
    public ResultUtil<Void> destroyUser(@RequestParam("userId") int userId) {
        return this.lockRun(() -> userUiService.destroyUser(userId));
    }

    @PermissionRequire(role = Constant.UserType.SUPPER_ADMIN)
    @PostMapping("/api/user/password/reset")
    @LoginRequire
    public ResultUtil<UserInfoModel> resetPassword(@RequestParam("userId") int userId, @RequestParam("password") String password) {
        return this.lockRun(() -> userUiService.resetPassword(userId, password));
    }

    @GetMapping("/api/user/list")
    public ResultUtil<List<UserInfoModel>> listUsers() {
        return this.lockRun(() -> userUiService.listUsers());
    }

    @GetMapping("/api/user/search")
    public ResultUtil<Page<UserInfoModel>> search(@RequestParam("keyword") String keyword,
                                                  @RequestParam("no") int no,
                                                  @RequestParam("size") int size) {

        return this.lockRun(() -> userUiService.search(keyword, no, size));
    }
}
