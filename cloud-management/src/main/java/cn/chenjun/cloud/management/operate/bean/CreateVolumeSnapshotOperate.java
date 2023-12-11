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
public class CreateVolumeSnapshotOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int snapshotVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_VOLUME_SNAPSHOT;
    }
}
