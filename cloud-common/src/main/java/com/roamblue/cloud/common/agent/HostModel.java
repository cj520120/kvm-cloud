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
@ApiModel("主机信息")
public class HostModel implements Serializable {
    @ApiModelProperty("主机名称")
    private String hostName;
    @ApiModelProperty("Libvirt版本")
    private long version;
    @ApiModelProperty("连接地址")
    private String uri;
    @ApiModelProperty("内存")
    private long memory;
    @ApiModelProperty("cpu")
    private int cpu;
    @ApiModelProperty("hypervisor类型")
    private String hypervisor;
}
