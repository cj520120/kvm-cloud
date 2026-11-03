package cn.ruiyun.cloud.management.operate.bean;

import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
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
    public String getTaskId() {
        return "Vol-Clone:" + sourceVolumeId + ":" + targetVolumeId;
    }
}
