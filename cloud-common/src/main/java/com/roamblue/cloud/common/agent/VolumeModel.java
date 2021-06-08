package com.roamblue.cloud.common.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("磁盘")
public class VolumeModel implements Serializable {
    @ApiModelProperty("存储池")
    private String storage;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("容量")
    private long capacity;
    @ApiModelProperty("已用")
    private long allocation;
}
