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
@ApiModel("模版下载信息")
public class TemplateRefInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("存储ID")
    private int storageId;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty("模版ID")
    private int templateId;
    @ApiModelProperty(value = "模版文件")
    private String target;
    @ApiModelProperty(value = "模版状态")
    private String status;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
