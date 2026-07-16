package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vlan网络
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VxLanNetworkRequest {
    private String poolId;
    private String cidr;
    private String gateway;
    private String baseUrl;
    private String token;
}
