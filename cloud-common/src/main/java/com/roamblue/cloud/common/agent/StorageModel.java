package com.roamblue.cloud.common.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("存储池")
public class StorageModel implements Serializable {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("状态")
    private String state;
    @ApiModelProperty("容量")
    private long capacity;
    @ApiModelProperty("可用")
    private long available;
    @ApiModelProperty("已用")
    private long allocation;
}
