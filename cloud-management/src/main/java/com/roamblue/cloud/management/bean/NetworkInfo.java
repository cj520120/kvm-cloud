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
@ApiModel("网络信息")
public class NetworkInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("网络名称")
    private String name;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty("管理分配开始IP")
    private String managerStartIp;
    @ApiModelProperty("管理分配结束IP")
    private String managerEndIp;
    @ApiModelProperty("虚拟机分配开始IP")
    private String guestStartIp;
    @ApiModelProperty("虚拟机分配结束IP")
    private String guestEndIp;
    @ApiModelProperty(value = "子网信息", example = "192.168.2.0")
    private String subnet;
    @ApiModelProperty(value = "网关", example = "192.168.2.1")
    private String gateway;
    @ApiModelProperty(value = "dns信息", example = "192.168.2.1,192.168.1.2,8.8.8.8")
    private String dns;
    @ApiModelProperty(value = "网卡名称", example = "br0")
    private String card;
    @ApiModelProperty(value = "网络类型", example = "Bridge")
    private String type;
    @ApiModelProperty(value = "网络状态", example = "Ready")
    private String status;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
