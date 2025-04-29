package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * 用户实体类
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoModel {

    /**
     * userId
     */
    private int userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 登录方式
     */
    private int loginType;
    /**
     * 用户类型
     */
    private int userType;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 密码
     */
    private String passwordSalt;
    /**
     * 状态
     */
    private short userStatus;
    /**
     * 注册时间
     */
    private Date registerTime;
}