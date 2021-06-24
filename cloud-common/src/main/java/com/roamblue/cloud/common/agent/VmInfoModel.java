package com.roamblue.cloud.common.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.libvirt.DomainInfo;

import java.io.Serializable;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("虚拟机信息")
public class VmInfoModel implements Serializable {
    @ApiModelProperty("虚拟机最大内存")
    public long maxMem;
    @ApiModelProperty("虚拟机内存Kib")
    public long memory;
    @ApiModelProperty("虚拟机cpu")
    public int cpu;
    @ApiModelProperty("虚拟机cpu时间")
    public long cpuTime;
    @ApiModelProperty("虚拟机名称")
    private String name;
    @ApiModelProperty("虚拟机UUID")
    private String uuid;
    @ApiModelProperty("VNC端口号")
    private int vnc;
    @ApiModelProperty("VNC密码")
    private String password;
    @ApiModelProperty("虚拟机状态")
    private DomainInfo.DomainState state;
}
