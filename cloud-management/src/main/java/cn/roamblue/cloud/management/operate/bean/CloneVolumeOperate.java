package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CloneVolumeOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetStorageId;
    private String targetName;
    private String targetPath;
    private String targetType;
}
