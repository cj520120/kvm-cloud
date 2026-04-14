package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubnetNetwork {
    private String subnet;      // 子网地址
    private String mask;        // 子网掩码
    private String gateway;     // 网关（一般=第一个可用IP）
    private String firstIp;     // 开始可用IP
    private String lastIp;      // 结束可用IP
    private String broadcast;   // 广播地址
    private long hostCount;
}
