package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("VNC信息")
public class VncInfo implements Serializable {

    @ApiModelProperty("主机地址")
    private String ip;
    @ApiModelProperty("token")
    private String token;
    @ApiModelProperty("password")
    private String password;
}
