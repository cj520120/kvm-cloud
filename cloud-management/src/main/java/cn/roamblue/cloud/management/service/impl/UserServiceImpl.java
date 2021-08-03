package cn.roamblue.cloud.management.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.bean.LoginUserInfo;
import cn.roamblue.cloud.management.bean.LoginUserTokenInfo;
import cn.roamblue.cloud.management.bean.UserInfo;
import cn.roamblue.cloud.management.data.entity.LoginInfoEntity;
import cn.roamblue.cloud.management.data.mapper.LoginInfoMapper;
import cn.roamblue.cloud.management.service.UserService;
import cn.roamblue.cloud.management.util.BeanConverter;
import cn.roamblue.cloud.management.util.UserState;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public LoginUserTokenInfo login(String loginName, String password, String nonce) {
        LoginInfoEntity loginInfoEntity = loginInfoMapper.findByLoginName(loginName);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, localeMessage.getMessage("USER_LOGIN_NAME_OR_PASSWORD_ERROR", "用户名或密码错误"));
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equals(password)) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, localeMessage.getMessage("USER_LOGIN_NAME_OR_PASSWORD_ERROR", "用户名或密码错误"));
        }
        if (loginInfoEntity.getLoginState() != UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, localeMessage.getMessage("USER_FORBID_ERROR", "用户已禁用"));
        }
        return getToken(loginInfoEntity.getUserId(), loginInfoEntity.getLoginPassword());
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
    public Integer getUserIdByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
        try {
            return Integer.parseInt(JWT.decode(token).getAudience().get(0));
        } catch (JWTDecodeException j) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
    }

    @Override
    public Integer verify(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
        Integer userId = this.getUserIdByToken(token);
        if (userId == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(loginInfoEntity.getLoginPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_VERIFY_ERROR", "用户验证失败,请重新登录"));
        }
        if (loginInfoEntity.getLoginState() != UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, localeMessage.getMessage("USER_FORBID_ERROR", "用户已禁用"));
        }
        return loginInfoEntity.getUserId();
    }

    @Override
    public LoginUserTokenInfo updatePassword(Integer userId, String oldPassword, String newPassword, String nonce) {

        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "登陆用户不存在"));
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equalsIgnoreCase(oldPassword)) {
            throw new CodeException(ErrorCode.OLD_PASSWORD_ERROR, localeMessage.getMessage("USER_OID_PASSWORD_ERROR", "旧密码错误"));
        }
        if (StringUtils.isEmpty(newPassword)) {
            throw new CodeException(ErrorCode.PASSWORD_EMPTY_ERROR, localeMessage.getMessage("USER_NEW_PASSWORD_EMPTY", "新密码不能为空"));
        }
        loginInfoEntity.setLoginPassword(newPassword);
        loginInfoMapper.updateById(loginInfoEntity);
        return getToken(loginInfoEntity.getUserId(), loginInfoEntity.getLoginPassword());

    }

    @Override
    public LoginUserTokenInfo refreshToken(Integer userId) {

        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "登陆用户不存在"));
        }
        if (loginInfoEntity.getLoginState() != UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, localeMessage.getMessage("USER_FORBID_ERROR", "用户已禁用"));
        }
        return getToken(loginInfoEntity.getUserId(), loginInfoEntity.getLoginPassword());
    }

    @Override
    public UserInfo register(String loginName, String password, int rule) {

        LoginInfoEntity entity = loginInfoMapper.findByLoginName(loginName);
        if (entity != null) {
            throw new CodeException(ErrorCode.LOGIN_USER_EXISTS, localeMessage.getMessage("USER_EXISTS", "用户已经存在"));
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = LoginInfoEntity.builder().loginState(UserState.ABLE).loginName(loginName).ruleType(rule).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        loginInfoMapper.insert(entity);
        return this.initUserInfoBO(entity);
    }

    @Override
    public UserInfo updateUserState(int userId, short state) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "登陆用户不存在"));
        }
        loginInfoEntity.setLoginState(state);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return this.initUserInfoBO(loginInfoEntity);
    }

    @Override
    public UserInfo updateUserRule(int userId, int rule) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "登陆用户不存在"));
        }
        loginInfoEntity.setRuleType(rule);
        this.loginInfoMapper.updateById(loginInfoEntity);
        return this.initUserInfoBO(loginInfoEntity);
    }

    @Override
    public List<UserInfo> listUsers() {
        List<LoginInfoEntity> list = this.loginInfoMapper.findAll();
        return BeanConverter.convert(list, this::initUserInfoBO);
    }

    @Override
    public UserInfo resetPassword(int userId, String password) {
        LoginInfoEntity loginInfoEntity = this.loginInfoMapper.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, localeMessage.getMessage("USER_NOT_FOUND", "登陆用户不存在"));
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

    private LoginUserTokenInfo getToken(int userId, String pwd) {
        Date expire = new Date(System.currentTimeMillis() + HOUR);
        return LoginUserTokenInfo.builder().expire(expire).token(JWT.create().withAudience(userId + "").withExpiresAt(expire).sign(Algorithm.HMAC256(pwd))).build();
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
        userModel.setRule(loginInfoEntity.getRuleType());
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
