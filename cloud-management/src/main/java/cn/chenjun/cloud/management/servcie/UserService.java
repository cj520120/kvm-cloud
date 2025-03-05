package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.UserInfoEntity;
import cn.chenjun.cloud.management.data.mapper.UserInfoMapper;
import cn.chenjun.cloud.management.model.LoginSignatureModel;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.model.TokenModel;
import cn.chenjun.cloud.management.model.UserInfoModel;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.Constant;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.DigestUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Service
public class UserService extends AbstractService {
    @Autowired
    private UserInfoMapper loginInfoMapper;

    @Autowired
    private ConfigService configService;


    public ResultUtil<TokenModel> login(String loginName, String password, String nonce) {

        UserInfoEntity loginInfoEntity = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equals(password)) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        if (loginInfoEntity.getLoginState() != Constant.UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return ResultUtil.success(getToken(Constant.UserType.LOCAL, loginInfoEntity.getUserId()));
    }

    public ResultUtil<TokenModel> loginOauth2(Object id) {
        return ResultUtil.success(this.getToken(Constant.UserType.OAUTH2, id));

    }

    public ResultUtil<UserInfoModel> findUserById(int userId) {
        return ResultUtil.success(this.initLoginInfoBO(loginInfoMapper.selectById(userId)));
    }


    public ResultUtil<UserInfoModel> findUserByLoginName(String loginName) {
        UserInfoEntity user = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        return ResultUtil.success(this.initLoginInfoBO(user));
    }

    public ResultUtil<LoginUserModel> getUserIdByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "token不能为空");
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256((String) this.configService.getConfig(ConfigKey.LOGIN_JWD_PASSWORD))).withIssuer((String) this.configService.getConfig(ConfigKey.LOGIN_JWD_ISSUER)).build();
            DecodedJWT jwt = jwtVerifier.verify(token);
            LoginUserModel loginUser = GsonBuilderUtil.create().fromJson(jwt.getClaim("User").asString(), LoginUserModel.class);
            boolean isEnableOauth2 = Objects.equals(this.configService.getConfig(ConfigKey.OAUTH2_ENABLE), Constant.Enable.YES);
            if (isEnableOauth2 && !Constant.UserType.OAUTH2.equals(loginUser.getType())) {
                throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "Token过期");
            } else if (!isEnableOauth2 && !Constant.UserType.LOCAL.equals(loginUser.getType())) {
                throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "Token过期");
            }
            return ResultUtil.success(loginUser);
        } catch (Exception j) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "Token过期");
        }
    }


    public ResultUtil<TokenModel> updatePassword(Integer userId, String oldPassword, String newPassword, String nonce) {

        UserInfoEntity loginInfoEntity = this.loginInfoMapper.selectById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equalsIgnoreCase(oldPassword)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "旧密码错误");
        }
        if (StringUtils.isEmpty(newPassword)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }
        loginInfoEntity.setLoginPassword(newPassword);
        loginInfoMapper.updateById(loginInfoEntity);
        return ResultUtil.success(getToken(Constant.UserType.LOCAL, loginInfoEntity.getUserId()));

    }


    public ResultUtil<TokenModel> refreshToken(LoginUserModel loginUser) {
        return ResultUtil.success(getToken(loginUser.getType(), loginUser.getId()));
    }


    public ResultUtil<UserInfoModel> register(String loginName, String password) {


        UserInfoEntity entity = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        if (entity != null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "用户已经存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = UserInfoEntity.builder().loginState(Constant.UserState.ABLE).loginName(loginName).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        loginInfoMapper.insert(entity);
        return ResultUtil.success(this.initUserInfoBO(entity));
    }

    public ResultUtil<UserInfoModel> updateUserState(int userId, short state) {

        UserInfoEntity loginInfoEntity = this.loginInfoMapper.selectById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        loginInfoEntity.setLoginState(state);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return ResultUtil.success(this.initUserInfoBO(loginInfoEntity));
    }


    public ResultUtil<List<UserInfoModel>> listUsers() {

        List<UserInfoEntity> list = this.loginInfoMapper.selectList(new QueryWrapper<>());
        return ResultUtil.success(list.stream().map(this::initUserInfoBO).collect(Collectors.toList()));
    }


    public ResultUtil<UserInfoModel> resetPassword(int userId, String password) {
        UserInfoEntity loginInfoEntity = this.loginInfoMapper.selectById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        loginInfoEntity.setLoginPassword(pwd);
        loginInfoEntity.setLoginPasswordSalt(salt);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return ResultUtil.success(this.initUserInfoBO(loginInfoEntity));
    }


    public ResultUtil<Void> destroyUser(int userId) {
        this.loginInfoMapper.deleteById(userId);
        return ResultUtil.success();
    }

    public ResultUtil<LoginSignatureModel> getSignature(String loginName) {
        if (StringUtils.isEmpty(loginName)) {
            return ResultUtil.error(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }

        UserInfoEntity loginInfoBean = this.loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        LoginSignatureModel model = LoginSignatureModel.builder().signature(loginInfoBean == null ? UUID.randomUUID().toString() : loginInfoBean.getLoginPasswordSalt()).nonce(String.valueOf(System.currentTimeMillis())).build();
        return ResultUtil.success(model);
    }

    public ResultUtil<LoginSignatureModel> getLoginSignature(Integer userId) {
        UserInfoEntity loginInfoBean = this.loginInfoMapper.selectById(userId);
        LoginSignatureModel model = LoginSignatureModel.builder().signature(loginInfoBean == null ? UUID.randomUUID().toString() : loginInfoBean.getLoginPasswordSalt()).nonce(String.valueOf(System.currentTimeMillis())).build();
        return ResultUtil.success(model);
    }

    private TokenModel getToken(String userType, Object userId) {
        int expireMinutes = this.configService.getConfig(ConfigKey.LOGIN_JWT_EXPIRE_MINUTES);
        Date expire = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expireMinutes));
        LoginUserModel user = LoginUserModel.builder().id(userId).type(userType).build();
        String token = JWT.create()
                .withIssuer(this.configService.getConfig(ConfigKey.LOGIN_JWD_ISSUER))
                .withIssuedAt(new Date())
                .withClaim("User", GsonBuilderUtil.create().toJson(user))
                .withExpiresAt(expire)
                .sign(Algorithm.HMAC256((String) this.configService.getConfig(ConfigKey.LOGIN_JWD_PASSWORD)));


        return TokenModel.builder().expire(expire).token(token).build();
    }

    private UserInfoModel initUserInfoBO(UserInfoEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        UserInfoModel userModel = new UserInfoModel();
        userModel.setUserId(loginInfoEntity.getUserId());
        userModel.setLoginName(loginInfoEntity.getLoginName());
        userModel.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        userModel.setState(loginInfoEntity.getLoginState());
        userModel.setRegisterTime(loginInfoEntity.getCreateTime());
        return userModel;
    }

    private UserInfoModel initLoginInfoBO(UserInfoEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        UserInfoModel loginInfoBean = new UserInfoModel();
        loginInfoBean.setUserId(loginInfoEntity.getUserId());
        loginInfoBean.setLoginName(loginInfoEntity.getLoginName());
        loginInfoBean.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        return loginInfoBean;
    }
}
