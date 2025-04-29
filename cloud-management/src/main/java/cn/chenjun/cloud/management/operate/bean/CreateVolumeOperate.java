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
public class CreateVolumeOperate extends BaseOperateParam {
    private int volumeId;
    private int templateId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_VOLUME;
    }

    @Override
    public String getId() {
        return "Vol-Create:" + volumeId;
    }
}
