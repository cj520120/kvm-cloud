package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeCreateTemplateRequest {
    private String sourceStorage;
    private String sourceVolume;
    private String targetName;
    private String targetStorage;
    private String targetVolume;
    private String targetType;

}
