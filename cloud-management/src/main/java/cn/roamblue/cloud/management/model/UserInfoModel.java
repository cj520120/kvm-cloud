package cn.roamblue.cloud.management.model;

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
     * 注册时间
     */
    private Date registerTime;
}