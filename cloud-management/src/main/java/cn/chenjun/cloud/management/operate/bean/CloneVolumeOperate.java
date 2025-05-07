package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
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
public class CloneVolumeOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.CLONE_VOLUME;
    }

    @Override
    public String getId() {
        return "Vol-Clone:" + sourceVolumeId + ":" + targetVolumeId;
    }
}
