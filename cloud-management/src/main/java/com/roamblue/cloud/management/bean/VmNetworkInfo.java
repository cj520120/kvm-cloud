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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("网卡信息")
public class VmNetworkInfo implements Serializable {
    @ApiModelProperty("网卡ID")
    private Integer id;
    @ApiModelProperty("所属网络")
    private Integer networkId;
    @ApiModelProperty("所属集群")
    private Integer clusterId;
    @ApiModelProperty("所属实例")
    private Integer vmId;
    @ApiModelProperty("设备")
    private Integer device;
    @ApiModelProperty("mac")
    private String mac;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("时间")
    private Date createTime;
}
