package cn.chenjun.cloud.management.operate.impl.cloud.bean;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SystemParamConfig {
    private String description;
    private Map<String, Integer> params;
    @SerializedName("support_networks")
    private List<Integer> supportNetworks;

    public static void main(String[] args) {

    }
}
