package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@ApiModel("登陆信息")
public class LoginUserTokenInfo {
    @ApiModelProperty("用户Token")
    private String token;
    @ApiModelProperty("过期时间")
    private Date expire;
}
