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
public class VolumeCreateRequest {
    private String parentStorage;
    private String parentVolume;
    private String parentType;
    private String targetName;
    private String targetStorage;
    private String targetVolume;
    private String targetType;
    private long targetSize;
}
