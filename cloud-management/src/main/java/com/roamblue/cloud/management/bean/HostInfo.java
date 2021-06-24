package com.roamblue.cloud.management.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("主机信息")
public class HostInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty("主机名称")
    private String name;
    @ApiModelProperty("主机地址")
    private String ip;
    @ApiModelProperty("agent地址")
    private String uri;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("主机内存")
    private long memory;
    @ApiModelProperty("主机核心数")
    private int cpu;
    @ApiModelProperty("已用内存")
    private long allocationMemory;
    @ApiModelProperty("已用核心数")
    private int allocationCpu;
    @ApiModelProperty("创建时间")
    private Date createTime;
}
