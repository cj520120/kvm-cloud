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
public class RebootGuestOperate extends BaseOperateParam {
    private int guestId;

    @Override
    public int getType() {
        return Constant.OperateType.REBOOT_GUEST;
    }

    @Override
    public String getId() {
        return "Vm-Reboot:" + guestId;
    }
}
