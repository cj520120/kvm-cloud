package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("用户信息")
public class UserInfo {

    /**
     * userId
     */
    @ApiModelProperty("ID")
    private int userId;
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String loginName;
    /**
     * 密码
     */
    @ApiModelProperty("密码")
    private String passwordSalt;
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private short state;
    /**
     * 权限
     */
    @ApiModelProperty("权限")
    private int rule;

    @ApiModelProperty("注册时间")
    private Date registerTime;
}