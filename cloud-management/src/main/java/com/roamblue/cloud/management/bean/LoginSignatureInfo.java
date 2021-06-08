package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("签名数据")
public class LoginSignatureInfo {
    @ApiModelProperty("签名串")
    private String signature;
    @ApiModelProperty("nonce")
    private String nonce;
}
