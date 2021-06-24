package com.roamblue.cloud.common.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("监控信息")
public class VmStaticsModel implements Serializable {
    @ApiModelProperty("虚拟机名称")
    private String name;
    @ApiModelProperty("时间")
    private long time;
    @ApiModelProperty("磁盘读取速率")
    private long diskReadSpeed;
    @ApiModelProperty("磁盘写入速率")
    private long diskWriteSpeed;
    @ApiModelProperty("网络发送速率")
    private long networkSendSpeed;
    @ApiModelProperty("网络接收速率")
    private long networkReceiveSpeed;
    @ApiModelProperty("cpu使用率")
    private int cpuUsage;
}
