package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.Page;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.UserInfoEntity;
import cn.chenjun.cloud.management.data.mapper.UserInfoMapper;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    @Autowired
    private RedissonClient redissonClient;


    public ResultUtil<TokenModel> login(String loginName, String password, String nonce) {

        UserInfoEntity loginInfoEntity = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName).eq(UserInfoEntity.LOGIN_TYPE, Constant.LoginType.LOCAL));
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
        return ResultUtil.success(buildToken(loginInfoEntity));
    }

    public ResultUtil<TokenModel> loginOauth2(String id, String name) {
        UserInfoEntity loginInfoEntity = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, id).eq(UserInfoEntity.LOGIN_TYPE, Constant.LoginType.OAUTH2));
        if (loginInfoEntity == null) {
            loginInfoEntity = new UserInfoEntity();
            loginInfoEntity.setLoginName(id);
            loginInfoEntity.setUserName(name);
            loginInfoEntity.setLoginPassword("");
            loginInfoEntity.setUserStatus(Constant.UserState.ABLE);
            loginInfoEntity.setLoginType(Constant.LoginType.OAUTH2);
            loginInfoEntity.setLoginPasswordSalt("");
            loginInfoEntity.setUserType(Constant.UserType.USER);
            loginInfoEntity.setCreateTime(new Date());
            loginInfoMapper.insert(loginInfoEntity);
        } else if (loginInfoEntity.getUserStatus() != Constant.UserState.ABLE) {
            throw new CodeException(ErrorCode.USER_FORBID_ERROR, "用户已禁用");
        }
        return ResultUtil.success(this.buildToken(loginInfoEntity));
    }

    public ResultUtil<UserInfoModel> findUserById(int userId) {
        return ResultUtil.success(this.initUserModel(loginInfoMapper.selectById(userId)));
    }


    public ResultUtil<UserInfoModel> findUserByLoginName(String loginName) {
        UserInfoEntity user = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        return ResultUtil.success(this.initUserModel(user));
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


    public ResultUtil<Void> updateSelfInfo(Integer userId, String userName, String oldPassword, String newPassword, String nonce) {

        UserInfoEntity loginInfoEntity = this.loginInfoMapper.selectById(userId);
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
        loginInfoMapper.updateById(loginInfoEntity);
        this.notifyService.publish(NotifyData.<Void>builder().id(userId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());
        return ResultUtil.success();

    }


    public ResultUtil<RefreshTokenModel> refreshToken(int userId) {
        RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userId));
        userToken.expire(1, TimeUnit.HOURS);
        return ResultUtil.success(RefreshTokenModel.builder().self(getUserInfo(userId).getData()).expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))).build());
    }


    public ResultUtil<UserInfoModel> register(String userName, String loginName, String password, short userType, short userStatus) {
        UserInfoEntity entity = loginInfoMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq(UserInfoEntity.LOGIN_NAME, loginName));
        if (entity != null) {
            throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户已经存在");
        }
        String salt = "CRY:" + RandomStringUtils.randomAlphanumeric(16);
        String pwd = DigestUtil.sha256Hex(password + ":" + salt);
        entity = UserInfoEntity.builder().userStatus(userStatus).userName(userName).loginType(Constant.LoginType.LOCAL).userType(userType).loginName(loginName).loginPasswordSalt(salt).loginPassword(pwd).createTime(new Date()).build();
        loginInfoMapper.insert(entity);
        this.notifyService.publish(NotifyData.<Void>builder().id(entity.getUserId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());
        return ResultUtil.success(this.initUserModel(entity));
    }

    public ResultUtil<UserInfoModel> updateUser(int userId, String userName, short userType, short state) {
        UserInfoEntity loginInfoEntity = this.loginInfoMapper.selectById(userId);
        if (loginInfoEntity == null) {
            throw new CodeException(ErrorCode.USER_NOT_FOUND, "登陆用户不存在");
        }
        loginInfoEntity.setUserName(userName);
        loginInfoEntity.setUserStatus(state);
        loginInfoEntity.setUserType(userType);
        this.loginInfoMapper.updateById(loginInfoEntity);
        if (state == Constant.UserState.DISABLE) {
            RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userId));
            userToken.delete();
        }
        RBucket<UserInfoModel> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userId));
        rUserInfo.delete();

        this.notifyService.publish(NotifyData.<Void>builder().id(userId).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_USER).build());
        return ResultUtil.success(this.initUserModel(loginInfoEntity));
    }


    public ResultUtil<List<UserInfoModel>> listUsers() {

        List<UserInfoEntity> list = this.loginInfoMapper.selectList(new QueryWrapper<>());
        return ResultUtil.success(list.stream().map(this::initUserModel).collect(Collectors.toList()));
    }

    public ResultUtil<Page<UserInfoModel>> search(String keyword, int no, int size) {
        QueryWrapper<UserInfoEntity> queryWrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(keyword)) {
            String condition = "%" + keyword + "%";
            queryWrapper.like(UserInfoEntity.LOGIN_NAME, condition);

        }
        int nCount = Math.toIntExact(this.loginInfoMapper.selectCount(queryWrapper));
        int nOffset = (no - 1) * size;
        queryWrapper.last("limit " + nOffset + ", " + size);
        List<UserInfoEntity> list = this.loginInfoMapper.selectList(queryWrapper);
        List<UserInfoModel> models = list.stream().map(this::initUserModel).collect(Collectors.toList());
        Page<UserInfoModel> page = Page.create(nCount, nOffset, size);
        page.setList(models);
        return ResultUtil.success(page);
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
        return ResultUtil.success(this.initUserModel(loginInfoEntity));
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

    private TokenModel buildToken(UserInfoEntity userInfo) {
        String token = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        RBucket<String> userToken = redissonClient.getBucket(RedisKeyUtil.getUserToken(userInfo.getUserId()));
        userToken.set(token, 1, TimeUnit.HOURS);
        RBucket<Integer> tokenUser = redissonClient.getBucket(RedisKeyUtil.getTokenUser(token));
        tokenUser.set(userInfo.getUserId(), 1, TimeUnit.HOURS);
        UserInfoModel self = initUserModel(userInfo);
        RBucket<UserInfoModel> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userInfo.getUserId()));
        rUserInfo.set(self, 7, TimeUnit.DAYS);
        return TokenModel.builder().expire(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)))
                .token(token).self(self).build();
    }

    public ResultUtil<UserInfoModel> getUserInfo(int userId) {
        RBucket<UserInfoModel> rUserInfo = redissonClient.getBucket(RedisKeyUtil.getUserInfo(userId));
        if (rUserInfo.isExists()) {
            return ResultUtil.success(rUserInfo.get());
        } else {
            UserInfoEntity user = loginInfoMapper.selectById(userId);
            if (user != null) {
                UserInfoModel model = initUserModel(user);
                rUserInfo.set(model, 7, TimeUnit.DAYS);
                return ResultUtil.success(model);
            } else {
                throw new CodeException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
        }
    }

    private UserInfoModel initUserModel(UserInfoEntity loginInfoEntity) {
        if (loginInfoEntity == null) {
            return null;
        }
        UserInfoModel userModel = new UserInfoModel();
        userModel.setUserId(loginInfoEntity.getUserId());
        userModel.setUserName(loginInfoEntity.getUserName());
        userModel.setLoginType(loginInfoEntity.getLoginType());
        userModel.setUserType(loginInfoEntity.getUserType());
        userModel.setLoginName(loginInfoEntity.getLoginName());
        userModel.setPasswordSalt(loginInfoEntity.getLoginPasswordSalt());
        userModel.setUserStatus(loginInfoEntity.getUserStatus());
        userModel.setRegisterTime(loginInfoEntity.getCreateTime());
        return userModel;
    }


    public boolean verifyPermission(int userId, int role) {
        ResultUtil<UserInfoModel> selfInfo = this.getUserInfo(userId);
        if (selfInfo.getCode() != ErrorCode.SUCCESS) {
            return false;
        }
        switch (role) {
            case Constant.UserType.SUPPER_ADMIN:
                return selfInfo.getData().getUserType() == Constant.UserType.SUPPER_ADMIN;
            case Constant.UserType.ADMIN:
                return selfInfo.getData().getUserType() == Constant.UserType.ADMIN || selfInfo.getData().getUserType() == Constant.UserType.SUPPER_ADMIN;
            default:
                return true;
        }
    }
}
