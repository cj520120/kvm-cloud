package cn.roamblue.cloud.management.v2.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloneVolumeOperate extends BaseOperateInfo {
    private int id;
    private int targetStorageId;
    private String targetName;
    private String targetVolume;
    private String targetType;
}
