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
public class StopGuestOperate extends BaseOperateParam {
    private int guestId;
    private boolean force;
    private boolean destroy;

    @Override
    public int getType() {
        return Constant.OperateType.STOP_GUEST;
    }

    @Override
    public String getId() {
        return "Vm-Destroy:" + guestId + ":" + force + ":" + destroy;
    }
}
