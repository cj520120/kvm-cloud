package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("群组信息")
public class GroupInfo {

    @ApiModelProperty("ID")
    private int id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("创建时间")
    private Date createTime;
}
