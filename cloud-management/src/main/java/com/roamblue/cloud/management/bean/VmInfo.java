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
@ApiModel("集群信息")
public class VmInfo implements Serializable {
    @ApiModelProperty("虚拟机ID")
    private int id;
    @ApiModelProperty("虚拟机集群")
    private int clusterId;
    @ApiModelProperty("主机ID")
    private int hostId;
    @ApiModelProperty("群组ID")
    private int groupId;
    @ApiModelProperty("计算方案")
    private int calculationSchemeId;
    @ApiModelProperty("光盘")
    private int iso;
    @ApiModelProperty("模版ID")
    private int templateId;
    @ApiModelProperty("IP")
    private String ip;
    @ApiModelProperty("VNC端口")
    private int vncPort;
    @ApiModelProperty("VNC密码")
    private String vncPassword;
    @ApiModelProperty("虚拟机名称")
    private String name;
    @ApiModelProperty("虚拟机备注")
    private String description;
    @ApiModelProperty("虚拟机类型")
    private String type;
    @ApiModelProperty("虚拟机状态")
    private String status;
    @ApiModelProperty("虚拟机状态")
    private Date createTime;
}
