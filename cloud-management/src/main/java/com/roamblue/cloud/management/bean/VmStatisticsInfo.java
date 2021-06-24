package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("Vm统计信息")
public class VmStatisticsInfo {
    @ApiModelProperty("创建时间")
    private Date time;
    @ApiModelProperty("磁盘读取速率")
    private float read;
    @ApiModelProperty("磁盘写入速率")
    private float write;
    @ApiModelProperty("网卡发送速率")
    private float send;
    @ApiModelProperty("网卡接收速率")
    private float receive;
    @ApiModelProperty("CPU使用率")
    private float cpu;
}
