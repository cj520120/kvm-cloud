package cn.chenjun.cloud.management.ovn.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortInfoData {

    private String mac;

    @SerializedName("port_uuid")
    private String portUuid;

    @SerializedName("port_name")
    private String portName;

    @SerializedName("user_bridge_name")
    private String userBridgeName;

    @SerializedName("ovn_bridge_name")
    private String ovnBridgeName;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName("created_at")
    private String createdAt;
}
