package cn.ruiyun.cloud.management.servcie;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.DigestUtil;
import cn.ruiyun.cloud.common.bean.Page;
import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.dao.UserInfoDao;
import cn.ruiyun.cloud.management.data.entity.UserEntity;
import cn.ruiyun.cloud.management.model.LoginUserModel;
import cn.ruiyun.cloud.management.servcie.bean.RefreshTokenInfo;
import cn.ruiyun.cloud.management.servcie.bean.TokenInfo;
import cn.ruiyun.cloud.management.servcie.bean.UserSignatureInfo;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import org.apache.commons.lang3.RandomStringUtils;
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
    private CacheService cacheService;


    public TokenInfo login(String loginName, String password, String nonce) {

        UserEntity loginInfoEntity = userDao.findByLoginNameAndLoginType(Constant.LoginType.LOCAL, loginName);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        String realPassword = DigestUtil.sha256Hex(loginInfoEntity.getLoginPassword() + ":" + nonce);
        if (!realPassword.equals(password)) {
            throw new CodeException(ErrorCode.USER_LOGIN_NAME_OR_PASSWORD_ERROR, "用户名或密码错误");
        }
        if (loginInfoEntity.getUserStatus() != Constant.UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return buildToken(loginInfoEntity);
    }

    public TokenInfo loginOauth2(String id, String name) {
        UserEntity loginInfoEntity = userDao.findByLoginNameAndLoginType(Constant.LoginType.OAUTH2, id);
        if (loginInfoEntity == null) {
            loginInfoEntity = new UserEntity();
            loginInfoEntity.setLoginName(id);
            loginInfoEntity.setUserName(name);
            loginInfoEntity.setLoginPassword("");
            loginInfoEntity.setUserStatus(Constant.UserState.ABLE);
            loginInfoEntity.setLoginType(Constant.LoginType.OAUTH2);
            loginInfoEntity.setLoginPasswordSalt("");
            loginInfoEntity.setUserType(Constant.UserType.USER);
            loginInfoEntity.setCreateTime(new Date());
            userDao.insert(loginInfoEntity);
        } else if (loginInfoEntity.getUserStatus() != Constant.UserState.ABLE) {
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
            Integer userId = cacheService.get(CacheKeyUtil.getTokenUser(token));
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
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(userId).type(Constant.NotifyType.UPDATE_USER).build());


    }


    public RefreshTokenInfo refreshToken(int userId) {
        String token = this.cacheService.get(CacheKeyUtil.getUserToken(userId));
        this.cacheService.expire(CacheKeyUtil.getTokenUser(token), TimeUnit.HOURS.toMillis(1));
        return RefreshTokenInfo.builder().self(getUserInfo(userId)).expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))).build();
    }


    public UserEntity register(String userName, String loginName, String password, short userType, short userStatus) {
        UserEntity entity = userDao.findByLoginNameAndLoginType(Constant.LoginType.LOCAL, loginName);
        if (entity != null) {
            throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户已经存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = UserEntity.builder().userStatus(userStatus).userName(userName).loginType(Constant.LoginType.LOCAL).userType(userType).loginName(loginName).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        userDao.insert(entity);
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(entity.getUserId()).type(Constant.NotifyType.UPDATE_USER).build());
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
        if (state == Constant.UserState.DISABLE) {

            this.cacheService.delete(CacheKeyUtil.getUserToken(userId));
        }
        this.cacheService.delete(CacheKeyUtil.getUserInfo(userId));

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(userId).type(Constant.NotifyType.UPDATE_USER).build());
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
        return this.cacheService.get(CacheKeyUtil.getUserInfo(userId), () -> {
            UserEntity findUser = userDao.findById(userId);
            if (findUser != null) {
                return findUser;
            } else {
                throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
        });
    }


    public boolean verifyPermission(int userId, int role) {
        UserEntity selfInfo = this.getUserInfo(userId);
        if (selfInfo == null) {
            return false;
        }
        switch (role) {
            case Constant.UserType.SUPPER_ADMIN:
                return selfInfo.getUserType() == Constant.UserType.SUPPER_ADMIN;
            case Constant.UserType.ADMIN:
                return selfInfo.getUserType() == Constant.UserType.ADMIN || selfInfo.getUserType() == Constant.UserType.SUPPER_ADMIN;
            default:
                return true;
        }
    }

    private TokenInfo buildToken(UserEntity userInfo) {
        String token = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        this.cacheService.put(CacheKeyUtil.getUserToken(userInfo.getUserId()), token, TimeUnit.HOURS.toMillis(1));


        this.cacheService.put(CacheKeyUtil.getTokenUser(token), userInfo.getUserId(), TimeUnit.DAYS.toMillis(7));

        this.cacheService.put(CacheKeyUtil.getUserInfo(userInfo.getUserId()), userInfo, TimeUnit.DAYS.toMillis(7));

        return TokenInfo.builder().expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .token(token).self(userInfo).build();
    }
}
