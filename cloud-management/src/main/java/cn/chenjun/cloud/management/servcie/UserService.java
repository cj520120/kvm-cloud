package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.dao.UserInfoDao;
import cn.chenjun.cloud.management.data.entity.UserEntity;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.model.UserModel;
import cn.chenjun.cloud.management.servcie.bean.RefreshTokenInfo;
import cn.chenjun.cloud.management.servcie.bean.TokenInfo;
import cn.chenjun.cloud.management.servcie.bean.UserSignatureInfo;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.DigestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class UserService extends AbstractService {
    @Autowired
    private UserInfoDao userDao;

    @Autowired
    private RedissonClient redissonClient;


    public TokenInfo login(String loginName, String password, String nonce) {

        UserEntity loginInfoEntity = userDao.findByLoginNameAndLoginType(Constant.LoginType.LOCAL, loginName);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equals(password)) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        if (loginInfoEntity.getUserStatus() != cn.chenjun.cloud.common.util.Constant.UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return buildToken(loginInfoEntity);
    }

    public TokenInfo loginOauth2(String id, String name) {
        UserEntity loginInfoEntity = userDao.findByLoginNameAndLoginType(cn.chenjun.cloud.common.util.Constant.LoginType.OAUTH2, id);
        if (loginInfoEntity == null) {
            loginInfoEntity = new UserEntity();
            loginInfoEntity.setLoginName(id);
            loginInfoEntity.setUserName(name);
            loginInfoEntity.setLoginPassword("");
            loginInfoEntity.setUserStatus(cn.chenjun.cloud.common.util.Constant.UserState.ABLE);
            loginInfoEntity.setLoginType(cn.chenjun.cloud.common.util.Constant.LoginType.OAUTH2);
            loginInfoEntity.setLoginPasswordSalt("");
            loginInfoEntity.setUserType(cn.chenjun.cloud.common.util.Constant.UserType.USER);
            loginInfoEntity.setCreateTime(new Date());
            userDao.insert(loginInfoEntity);
        } else if (loginInfoEntity.getUserStatus() != cn.chenjun.cloud.common.util.Constant.UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return this.buildToken(loginInfoEntity);
    }

    public UserEntity findUserById(int userId) {
        return userDao.findById(userId);
    }


    public ResultUtil<LoginUserModel> getUserIdByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登录已过期");
        }
        try {
            RBucket<Integer> tokenUser = redissonClient.getBucket(RedisKeyUtil.getTokenUser(token));
            Integer userId = tokenUser.get();
            if (userId == null) {
                throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登录已过期");
            }
            return ResultUtil.success(LoginUserModel.builder().userId(userId).build());
        } catch (Exception j) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "Token过期");
        }
    }


    public void updateSelfInfo(Integer userId, String userName, String oldPassword, String newPassword, String nonce) {

        UserEntity loginInfoEntity = this.userDao.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        if (!ObjectUtils.isEmpty(userName)) {
            loginInfoEntity.setUserName(userName);
        }
        if (!ObjectUtils.isEmpty(oldPassword) || !ObjectUtils.isEmpty(newPassword)) {
            String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
            if (!realPassword.equalsIgnoreCase(oldPassword)) {
                throw new CodeException(ErrorCode.OLD_PASSWORD_ERROR, "旧密码错误");
            }
            if (StringUtils.isEmpty(newPassword)) {
                throw new CodeException(ErrorCode.PASSWORD_NOT_EMPTY, "新密码不能为空");
            }
        }
        loginInfoEntity.setLoginPassword(newPassword);
        userDao.update(loginInfoEntity);
        this.notifyService.publish(NotifyData.<Void>builder().id(userId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());


    }


    public RefreshTokenInfo refreshToken(int userId) {
        RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userId));
        userToken.expire(1, TimeUnit.HOURS);
        return RefreshTokenInfo.builder().self(getUserInfo(userId)).expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))).build();
    }


    public UserEntity register(String userName, String loginName, String password, short userType, short userStatus) {
        UserEntity entity = userDao.findByLoginNameAndLoginType(Constant.LoginType.LOCAL, loginName);
        if (entity != null) {
            throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户已经存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = UserEntity.builder().userStatus(userStatus).userName(userName).loginType(cn.chenjun.cloud.common.util.Constant.LoginType.LOCAL).userType(userType).loginName(loginName).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        userDao.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getUserId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());
        return entity;
    }

    public UserEntity updateUser(int userId, String userName, short userType, short state) {
        UserEntity loginInfoEntity = this.userDao.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_NOT_FOUND, "登陆用户不存在");
        }
        loginInfoEntity.setUserName(userName);
        loginInfoEntity.setUserStatus(state);
        loginInfoEntity.setUserType(userType);
        this.userDao.update(loginInfoEntity);
        if (state == cn.chenjun.cloud.common.util.Constant.UserState.DISABLE) {
            RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userId));
            userToken.delete();
        }
        RBucket<UserModel> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userId));
        rUserInfo.delete();

        this.notifyService.publish(NotifyData.<Void>builder().id(userId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());
        return loginInfoEntity;
    }


    public List<UserEntity> listUsers() {

        List<UserEntity> list = this.userDao.listAll();
        return list;
    }

    public Page<UserEntity> search(String keyword, int no, int size) {
        Page<UserEntity> page = this.userDao.search(keyword, no, size);
        return page;
    }

    public UserEntity resetPassword(int userId, String password) {
        UserEntity loginInfoEntity = this.userDao.findById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.NO_LOGIN_ERROR, "登陆用户不存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        loginInfoEntity.setLoginPassword(pwd);
        loginInfoEntity.setLoginPasswordSalt(salt);
        this.userDao.update(loginInfoEntity);
        return loginInfoEntity;
    }


    public void destroyUser(int userId) {
        this.userDao.deleteById(userId);

    }

    public UserSignatureInfo getSignature(String loginName) {
        if (StringUtils.isEmpty(loginName)) {
            throw new CodeException(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }

        UserEntity loginInfoBean = this.userDao.findByLoginNameAndLoginType(Constant.LoginType.LOCAL, loginName);
        UserSignatureInfo model = UserSignatureInfo.builder().signature(loginInfoBean == null ? UUID.randomUUID().toString() : loginInfoBean.getLoginPasswordSalt()).nonce(String.valueOf(System.currentTimeMillis())).build();
        return model;
    }

    public UserSignatureInfo getLoginSignature(Integer userId) {
        UserEntity loginInfoBean = this.userDao.findById(userId);
        UserSignatureInfo model = UserSignatureInfo.builder().signature(loginInfoBean == null ? UUID.randomUUID().toString() : loginInfoBean.getLoginPasswordSalt()).nonce(String.valueOf(System.currentTimeMillis())).build();
        return model;
    }


    public UserEntity getUserInfo(int userId) {
        RBucket<UserEntity> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userId));
        if (rUserInfo.isExists()) {
            return rUserInfo.get();
        } else {
            UserEntity user = userDao.findById(userId);
            if (user != null) {
                rUserInfo.set(user, 7, TimeUnit.DAYS);
                return user;
            } else {
                throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
        }
    }




    public boolean verifyPermission(int userId, int role) {
        UserEntity selfInfo = this.getUserInfo(userId);
        if (selfInfo == null) {
            return false;
        }
        switch (role) {
            case cn.chenjun.cloud.common.util.Constant.UserType.SUPPER_ADMIN:
                return selfInfo.getUserType() == cn.chenjun.cloud.common.util.Constant.UserType.SUPPER_ADMIN;
            case cn.chenjun.cloud.common.util.Constant.UserType.ADMIN:
                return selfInfo.getUserType() == cn.chenjun.cloud.common.util.Constant.UserType.ADMIN || selfInfo.getUserType() == Constant.UserType.SUPPER_ADMIN;
            default:
                return true;
        }
    }

    private TokenInfo buildToken(UserEntity userInfo) {
        String token = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userInfo.getUserId()));
        userToken.set(token, 1, TimeUnit.HOURS);
        RBucket<Integer> tokenUser = redissonClient.getBucket(RedisKeyUtil.getTokenUser(token));
        tokenUser.set(userInfo.getUserId(), 1, TimeUnit.HOURS);
        RBucket<UserEntity> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userInfo.getUserId()));
        rUserInfo.set(userInfo, 7, TimeUnit.DAYS);
        return TokenInfo.builder().expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .token(token).self(userInfo).build();
    }
}
