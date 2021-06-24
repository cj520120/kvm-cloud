package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户实体类
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("登陆用户信息")
public class LoginUserInfo {

    /**
     * userId
     */
    @ApiModelProperty("用户ID")
    private Integer userId;
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
}