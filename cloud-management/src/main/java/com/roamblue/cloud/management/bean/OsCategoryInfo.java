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
@ApiModel("系统类型")
public class OsCategoryInfo {

    @ApiModelProperty("ID")
    private int id;
    @ApiModelProperty("名称")
    private String categoryName;
    @ApiModelProperty("网卡驱动")
    private String networkDriver;
    @ApiModelProperty("磁盘驱动")
    private String diskDriver;
}
