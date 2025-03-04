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
public class MigrateTemplateVolumeOperate extends BaseOperateParam {
    private int sourceTemplateVolumeId;
    private int targetTemplateVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.MIGRATE_TEMPLATE_VOLUME;
    }
}
