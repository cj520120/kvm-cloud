package com.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_login_info")
public class LoginInfoEntity {
    @TableId(type = IdType.AUTO)
    @TableField("user_id")
    private Integer userId;

    /**
     * 用户名
     */
    @TableField("login_name")
    private String loginName;
    /**
     * 密码
     */
    @TableField("login_password")
    private String loginPassword;
    /**
     *
     */
    @TableField("login_state")
    private Short loginState;
    /**
     *
     */
    @TableField("rule_type")
    private int ruleType;
    /**
     * 密码
     */
    @TableField("login_password_salt")
    private String loginPasswordSalt;

    /**
     * 密码
     */
    @TableField("create_time")
    private Date createTime;
}
