package cn.chenjun.cloud.management.servcie.convert.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VCpuTune {
    private int vcpu;
    private int cpuset;
}
