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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StartComponentGuestOperate extends StartGuestOperate {
    private int componentType;

    @Override
    public int getType() {
        return Constant.OperateType.START_COMPONENT_GUEST;
    }

}
