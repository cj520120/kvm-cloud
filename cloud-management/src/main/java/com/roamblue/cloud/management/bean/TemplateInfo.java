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
@ApiModel("模版信息")
public class TemplateInfo implements Serializable {
    @ApiModelProperty("id")
    private int id;
    @ApiModelProperty("模版名称")
    private String name;
    @ApiModelProperty("集群ID")
    private int clusterId;
    @ApiModelProperty(value = "远程地址")
    private String uri;
    @ApiModelProperty(value = "模版类型")
    private String type;
    @ApiModelProperty(value = "系统类型ID")
    private int osCategoryId;
    @ApiModelProperty(value = "模版状态")
    private String status;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
