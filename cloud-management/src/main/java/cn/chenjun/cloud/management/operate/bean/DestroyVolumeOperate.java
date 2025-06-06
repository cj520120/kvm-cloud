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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DestroyVolumeOperate extends BaseOperateParam {
    private int volumeId;

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_VOLUME;
    }

    @Override
    public String getId() {
        return "Vol-Destroy:" + volumeId;
    }
}
