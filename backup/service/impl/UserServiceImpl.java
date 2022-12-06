package cn.roamblue.cloud.management.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.LoginUser;
import cn.roamblue.cloud.management.bean.LoginUserInfo;
import cn.roamblue.cloud.management.bean.LoginUserTokenInfo;
import cn.roamblue.cloud.management.bean.UserInfo;
import cn.roamblue.cloud.management.config.ApplicaionConfig;
import cn.roamblue.cloud.management.config.Oauth2Config;
import cn.roamblue.cloud.management.data.entity.LoginInfoEntity;
import cn.roamblue.cloud.management.data.mapper.LoginInfoMapper;
import cn.roamblue.cloud.management.service.RuleService;
import cn.roamblue.cloud.management.service.UserService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.UserState;
import cn.roamblue.cloud.management.util.UserType;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author chenjun
 */
@Service
public class UserServiceImpl extends AbstractService implements UserService {
    private static final int HOUR = 1000 * 60 * 60;
    @Autowired
    private LoginInfoMapper loginInfoMapper;

    @Autowired
    private Oauth2Config oauth2Config;
    @Autowired
    private ApplicaionConfig applicaionConfig;
    @Autowired
    private RuleService ruleService;
    @Override
    public LoginUserTokenInfo login(String loginName, String password, String nonce) {
        if(oauth2Config.isEnable()){
            throw new CodeException(ErrorCode.LOCAL_USER_NOT_SUPPORT, "启用Oauth2协议后不支持本地登录");
        }
        LoginInfoEntity loginInfoEntity = loginInfoMapper.findByLoginName(loginName);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equals(password)) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        if (loginInfoEntity.getLoginState() != UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return getToken(UserType.LOCAL,loginInfoEntity.getUserId(), ruleService.getUserPermissionList(loginInfoEntity.getUserId()));
    }
    @Override
    public LoginUserTokenInfo loginOauth2(Object id, Collection<String> authorities) {
        return this.getToken(UserType.OAURH2, id, authorities);

    }
    @Override
    public LoginUserInfo findUserById(int userId) {
        return this.initLoginInfoBO(loginInfoMapper.findById(userId));
    }

    @Override
    public LoginUserInfo findUserByLoginName(String loginName) {
        return this.initLoginInfoBO(loginInfoMapper.findByLoginName(loginName));
    }

    @Override
    public LoginUser getUserIdByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(this.applicaionConfig.getJwtPassword())).withIssuer(this.applicaionConfig.getJwtIssuer()).build();
            DecodedJWT jwt = jwtVerifier.verify(token);
            LoginUser loginUser = GsonBuilderUtil.create().fromJson(jwt.getClaim("User").asString(), LoginUser.class);
            if (this.oauth2Config.isEnable() && !UserType.OAURH2.equals(loginUser.getType())) {
                return null;
            } else if (!this.oauth2Config.isEnable() && !UserType.LOCAL.equals(loginUser.getType())) {
                return null;
            }
            return loginUser;
        } catch (Exception j) {
            return null;
        }
    }



    @Override
    public LoginUserTokenInfo updatePassword(Integer userId, String oldPassword, String newPassword, String nonce) {

        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equalsIgnoreCase(oldPassword)) {
            throw new CodeException(ErrorCode.OLD_PASSWORD_ERROR, "旧密码错误");
        }
        if (StringUtils.isEmpty(newPassword)) {
            throw new CodeException(ErrorCode.PASSWORD_EMPTY_ERROR, "新密码不能为空");
        }
        loginInfoEntity.setLoginPassword(newPassword);
        loginInfoMapper.updateById(loginInfoEntity);
        return getToken(UserType.LOCAL,loginInfoEntity.getUserId(), this.ruleService.getUserPermissionList(userId));

    }

    @Override
    public LoginUserTokenInfo refreshToken(LoginUser loginUser) {
        return getToken(loginUser.getType(), loginUser.getId(), loginUser.getAuthorities());
    }

    @Override
    public UserInfo register(String loginName, String password, int rule) {

        if(oauth2Config.isEnable()){
            throw new CodeException(ErrorCode.LOCAL_USER_NOT_SUPPORT, "启用Oauth2协议后不支持注册本地登录");
        }
        LoginInfoEntity entity = loginInfoMapper.findByLoginName(loginName);
        if (entity != null) {
            throw new CodeException(ErrorCode.LOGIN_USER_EXISTS,"用户已经存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = LoginInfoEntity.builder().loginState(UserState.ABLE).loginName(loginName).ruleId(rule).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        loginInfoMapper.insert(entity);
        return this.initUserInfoBO(entity);
    }

    @Override
    public UserInfo updateUserState(int userId, short state) {
        if(oauth2Config.isEnable()){
            throw new CodeException(ErrorCode.LOCAL_USER_NOT_SUPPORT, "启用Oauth2协议后不支持修改用户状态");
        }
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        loginInfoEntity.setLoginState(state);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return this.initUserInfoBO(loginInfoEntity);
    }

    @Override
    public UserInfo updateUserRule(int userId, int rule) {
        if(oauth2Config.isEnable()){
            throw new CodeException(ErrorCode.LOCAL_USER_NOT_SUPPORT, "启用Oauth2协议后不支持修改用户权限");
        }
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        loginInfoEntity.setRuleId(rule);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return this.initUserInfoBO(loginInfoEntity);
    }

    @Override
    public List<UserInfo> listUsers() {
        if(oauth2Config.isEnable()){
            throw new CodeException(ErrorCode.LOCAL_USER_NOT_SUPPORT, "启用Oauth2协议后不支持获取本地用户");
        }
        List<LoginInfoEntity> list = this.loginInfoMapper.findAll();
        return BeanConverter.convert(list, this::initUserInfoBO);
    }

    @Override
    public UserInfo resetPassword(int userId, String password) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        loginInfoEntity.setLoginPassword(pwd);
        loginInfoEntity.setLoginPasswordSalt(salt);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return this.initUserInfoBO(loginInfoEntity);
    }

    @Override
    public void destroyUser(int userId) {
        this.loginInfoMapper.deleteById(userId);
    }


    private LoginUserTokenInfo getToken(String userType, Object userId, Collection<String> authorities) {
        Date expire = new Date(System.currentTimeMillis() + HOUR);

        LoginUser user = LoginUser.builder().id(userId).type(userType).authorities(authorities).build();
        String token = JWT.create()
                .withIssuer(this.applicaionConfig.getJwtIssuer())
                .withIssuedAt(new Date())
                .withClaim("User", GsonBuilderUtil.create().toJson(user))
                .withExpiresAt(expire)
                .sign(Algorithm.HMAC256(this.applicaionConfig.getJwtPassword()));


        return LoginUserTokenInfo.builder().expire(expire).token(token).build();
    }
    private UserInfo initUserInfoBO(LoginInfoEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        UserInfo userModel = new UserInfo();
        userModel.setUserId(loginInfoEntity.getUserId());
        userModel.setLoginName(loginInfoEntity.getLoginName());
        userModel.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        userModel.setState(loginInfoEntity.getLoginState());
        userModel.setRule(loginInfoEntity.getRuleId());
        userModel.setRegisterTime(loginInfoEntity.getCreateTime());
        return userModel;
    }

    private LoginUserInfo initLoginInfoBO(LoginInfoEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        LoginUserInfo loginInfoBean = new LoginUserInfo();
        loginInfoBean.setUserId(loginInfoEntity.getUserId());
        loginInfoBean.setLoginName(loginInfoEntity.getLoginName());
        loginInfoBean.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        return loginInfoBean;
    }
}
