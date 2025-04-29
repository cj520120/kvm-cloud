package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.management.util.Constant;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrateVolumeOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.MIGRATE_VOLUME;
    }

    @Override
    public String getId() {
        return "Vol-Migrate:" + sourceVolumeId + ":" + targetVolumeId;
    }
}
