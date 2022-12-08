package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitHostRequest {
    private String clientId;
    private String clientSecret;
    private List<StorageCreateRequest> storageList;
    private List<BasicBridgeNetwork> basicBridgeNetworkList;
    private List<VlanNetwork> vlanNetworkList;
}
