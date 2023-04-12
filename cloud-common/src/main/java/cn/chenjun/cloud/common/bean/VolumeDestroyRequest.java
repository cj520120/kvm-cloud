package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Vlan网络
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeDestroyRequest {
    private String sourceStorage;
    private String sourceVolume;
    private String sourceType;
}
