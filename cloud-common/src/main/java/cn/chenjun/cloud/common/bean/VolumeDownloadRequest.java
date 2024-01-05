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
public class VolumeDownloadRequest {
    private String md5;
    private String sourceUri;
    private String targetStorage;
    private String targetName;
    private String targetType;
}
