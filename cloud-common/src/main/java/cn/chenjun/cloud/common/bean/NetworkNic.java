package cn.chenjun.cloud.common.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkNic {
    private String xml;
    private int type;
    private String poolId;
    private String cidr;
    private String gateway;
    private String model;
    private String ip;
    private String mac;
    private String baseUrl;
    private String token;
}
