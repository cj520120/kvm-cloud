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
@ApiModel("磁盘信息")
public class VolumeInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty(value = "存储池ID")
    private int storageId;
    @ApiModelProperty(value = "实例ID")
    private int vmId;
    @ApiModelProperty(value = "挂载设备")
    private int device;
    @ApiModelProperty(value = "路径")
    private String target;
    @ApiModelProperty(value = "存储名称")
    private String name;
    @ApiModelProperty(value = "磁盘状态")
    private String status;
    @ApiModelProperty("物理大小")
    private long capacity;
    @ApiModelProperty("申请容量")
    private long allocation;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
