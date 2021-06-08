package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("计算方案")
public class CalculationSchemeInfo implements Serializable {
    @ApiModelProperty("ID")
    private int id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("cpu")
    private int cpu;
    @ApiModelProperty("内存")
    private long memory;
    @ApiModelProperty("评率")
    private int speed;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
