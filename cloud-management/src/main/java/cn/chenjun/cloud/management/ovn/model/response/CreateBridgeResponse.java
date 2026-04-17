package cn.chenjun.cloud.management.ovn.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBridgeResponse {

    @SerializedName("app_bridge_name")
    private String appBridgeName;

    @SerializedName("ovn_bridge_uuid")
    private String ovnBridgeUuid;

    @SerializedName("ovn_bridge_name")
    private String ovnBridgeName;
}
