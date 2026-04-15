package cn.chenjun.cloud.management.ovn.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NicXmlData {

    private String xml;

    @SerializedName("port_name")
    private String portName;

    private String mac;

    @SerializedName("bridge_name")
    private String bridgeName;
}
