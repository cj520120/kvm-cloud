package cn.roamblue.cloud.management.bean;

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
public class UserInfo {

    /**
     * userId
     */
    private int userId;
    /**
     * 用户名
     */
    private String loginName;
    /**
     * 密码
     */
    private String passwordSalt;
    /**
     * 状态
     */
    private short state;
    /**
     * 权限
     */
    private int rule;
    /**
     * 注册时间
     */
    private Date registerTime;
}