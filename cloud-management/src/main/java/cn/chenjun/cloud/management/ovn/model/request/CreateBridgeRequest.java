package cn.chenjun.cloud.management.ovn.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBridgeRequest {

    private String name;

    private String cidr;

    private String gateway;
}
