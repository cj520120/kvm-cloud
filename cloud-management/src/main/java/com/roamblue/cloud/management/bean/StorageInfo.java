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
@ApiModel("存储池信息")
public class StorageInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("存储池名称")
    private String name;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty(value = "存储池地址")
    private String host;
    @ApiModelProperty(value = "存储池来源地址")
    private String source;
    @ApiModelProperty(value = "存储池挂载地址")
    private String target;
    @ApiModelProperty(value = "存储池状态")
    private String status;
    @ApiModelProperty("容量")
    private long capacity;
    @ApiModelProperty("可用")
    private long available;
    @ApiModelProperty("已用")
    private long allocation;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
