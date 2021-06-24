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
@ApiModel("集群信息")
public class ClusterInfo implements Serializable {
    @ApiModelProperty("集群ID")
    private int id;
    @ApiModelProperty("集群名称")
    private String name;
    @ApiModelProperty("cpu超配比例")
    private float overCpu;
    @ApiModelProperty("内存超配比例")
    private float overMemory;
    @ApiModelProperty("集群状态")
    private String status;
    @ApiModelProperty("创建时间")
    private Date createTime;
}
