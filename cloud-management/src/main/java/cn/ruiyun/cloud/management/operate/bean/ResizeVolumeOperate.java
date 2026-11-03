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
public class ResizeVolumeOperate extends BaseOperateParam {
    private int volumeId;
    private long size;

    @Override
    public int getType() {
        return Constant.OperateType.RESIZE_VOLUME;
    }

    @Override
    public String getTaskId() {
        return "Volume-Resize:" + volumeId + ":" + size;
    }
}
