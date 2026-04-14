package cn.chenjun.cloud.management.ovn.model.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildInterfaceXmlRequest {

    @SerializedName("bridge_name")
    private String bridgeName;

    private String mac;

    @SerializedName("pci_address")
    private String pciAddress;

    private String model;

    private String ip;
}
