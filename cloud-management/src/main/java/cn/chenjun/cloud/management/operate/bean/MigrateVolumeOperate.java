package cn.chenjun.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrateVolumeOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetVolumeId;
}
