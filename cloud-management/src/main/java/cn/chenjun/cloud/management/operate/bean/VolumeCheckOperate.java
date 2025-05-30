package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeCheckOperate extends BaseOperateParam {
    private int storageId;

    @Override
    public int getType() {
        return Constant.OperateType.VOLUME_CHECK;
    }

    @Override
    public String getTaskId() {
        return "VOLUME_CHECK:" + this.storageId;
    }
}
