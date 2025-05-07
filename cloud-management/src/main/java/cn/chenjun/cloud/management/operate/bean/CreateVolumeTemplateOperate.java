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
public class CreateVolumeTemplateOperate extends BaseOperateParam {
    private int sourceVolumeId;
    private int targetTemplateVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_VOLUME_TEMPLATE;
    }

    @Override
    public String getId() {
        return "Vol-CreateTemplate:" + sourceVolumeId + ":" + targetTemplateVolumeId;
    }
}
