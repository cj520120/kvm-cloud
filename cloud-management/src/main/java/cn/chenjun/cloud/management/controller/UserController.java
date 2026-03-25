package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.LoginRequire;
import cn.chenjun.cloud.common.core.annotation.NoLoginRequire;
import cn.chenjun.cloud.common.core.annotation.PermissionRequire;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.UserEntity;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.servcie.bean.RefreshTokenInfo;
import cn.chenjun.cloud.management.servcie.bean.TokenInfo;
import cn.chenjun.cloud.management.servcie.bean.UserSignatureInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        TokenInfo token = userUiService.login(loginName, password, nonce);
        TokenModel model = new TokenModel();
        model.setToken(token.getToken());
        model.setExpire(token.getExpire());
        model.setSelf(this.convertService.initUserModel(token.getSelf()));
        return ResultUtil.success(model);
    }

    @PostMapping("/api/user/self/modify")
    public ResultUtil<Void> updateSelfInfo(@RequestAttribute(cn.chenjun.cloud.common.util.Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model, @RequestParam(value = "username", defaultValue = "") String username, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("nonce") String nonce) {

        this.globalLockCall(() -> userUiService.updateSelfInfo(model.getUserId(), username, oldPassword, newPassword, nonce));
        return ResultUtil.success();
    }

    @PostMapping("/api/user/token/refresh")
    public ResultUtil<RefreshTokenModel> refreshToken(@RequestAttribute(cn.chenjun.cloud.common.util.Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel loginUser) {
        RefreshTokenInfo info = userUiService.refreshToken(loginUser.getUserId());
        RefreshTokenModel model = new RefreshTokenModel();
        model.setExpire(info.getExpire());
        model.setSelf(this.convertService.initUserModel(info.getSelf()));
        return ResultUtil.success(model);
    }

    @NoLoginRequire
    @GetMapping("/api/user/login/signature")
    public ResultUtil<LoginSignatureModel> getSignature(@RequestParam("loginName") String loginName) {
        UserSignatureInfo signatureInfo = userUiService.getSignature(loginName);
        LoginSignatureModel model = new LoginSignatureModel();
        model.setNonce(signatureInfo.getNonce());
        model.setSignature(signatureInfo.getSignature());
        return ResultUtil.success(model);
    }

    @GetMapping("/api/user/signature")
    public ResultUtil<LoginSignatureModel> getLoginSignature(@RequestAttribute(cn.chenjun.cloud.common.util.Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel current) {

        UserSignatureInfo signatureInfo = userUiService.getLoginSignature(current.getUserId());
        LoginSignatureModel model = new LoginSignatureModel();
        model.setNonce(signatureInfo.getNonce());
        model.setSignature(signatureInfo.getSignature());
        return ResultUtil.success(model);
    }


    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.SUPPER_ADMIN)
    @PutMapping("/api/user/register")
    public ResultUtil<UserModel> register(@RequestParam("userName") String userName, @RequestParam("loginName") String loginName, @RequestParam("password") String password, @RequestParam("userType") short userType, @RequestParam("userStatus") short userStatus) {

        UserEntity user = this.globalLockCall(() -> userUiService.register(userName, loginName, password, userType, userStatus));
        return ResultUtil.success(this.convertService.initUserModel(user));

    }

    @GetMapping("/api/user/self")
    public ResultUtil<UserModel> getSelfInfo(@RequestAttribute(cn.chenjun.cloud.common.util.Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE) LoginUserModel model) {

        UserEntity user = userUiService.getUserInfo(model.getUserId());
        return ResultUtil.success(this.convertService.initUserModel(user));

    }

    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.SUPPER_ADMIN)
    @PostMapping("/api/user/update")
    @LoginRequire
    public ResultUtil<UserModel> updateUserInfo(@RequestParam("userId") int userId, @RequestParam("userName") String userName, @RequestParam("userType") short userType, @RequestParam("userStatus") short userStatus) {
        UserEntity user = this.globalLockCall(() -> userUiService.updateUser(userId, userName, userType, userStatus));
        return ResultUtil.success(this.convertService.initUserModel(user));
    }


    @PermissionRequire(role = cn.chenjun.cloud.common.util.Constant.UserType.SUPPER_ADMIN)
    @DeleteMapping("/api/user/destroy")
    public ResultUtil<Void> destroyUser(@RequestParam("userId") int userId) {
        this.globalLockCall(() -> userUiService.destroyUser(userId));
        return ResultUtil.success();
    }

    @PermissionRequire(role = Constant.UserType.SUPPER_ADMIN)
    @PostMapping("/api/user/password/reset")
    @LoginRequire
    public ResultUtil<UserModel> resetPassword(@RequestParam("userId") int userId, @RequestParam("password") String password) {
        UserEntity user = this.globalLockCall(() -> userUiService.resetPassword(userId, password));
        return ResultUtil.success(this.convertService.initUserModel(user));
    }

    @GetMapping("/api/user/list")
    public ResultUtil<List<UserModel>> listUsers() {


        List<UserEntity> users = userUiService.listUsers();
        List<UserModel> models = users.stream().map(this.convertService::initUserModel).collect(Collectors.toList());
        return ResultUtil.success(models);
    }

    @GetMapping("/api/user/search")
    public ResultUtil<Page<UserModel>> search(@RequestParam("keyword") String keyword,
                                              @RequestParam("no") int no,
                                              @RequestParam("size") int size) {

        Page<UserEntity> page = userUiService.search(keyword, no, size);
        return ResultUtil.success(Page.convert(page, this.convertService::initUserModel));

    }
}
