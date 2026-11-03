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
public class CreateVolumeTemplateOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetTemplateVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_VOLUME_TEMPLATE;
    }

    @Override
    public String getTaskId() {
        return "Vol-CreateTemplate:" + sourceVolumeId + ":" + targetTemplateVolumeId;
    }
}
