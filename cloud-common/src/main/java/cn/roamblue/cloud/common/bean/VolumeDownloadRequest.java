package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeDownloadRequest {
    private String sourceUri;
    private String targetStorage;
    private String targetName;
    private String targetVolume;
    private String targetType;
}
