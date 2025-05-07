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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StartGuestOperate extends BaseOperateParam {
    private int guestId;
    private int hostId;

    @Override
    public int getType() {
        return Constant.OperateType.START_GUEST;
    }

    @Override
    public String getTaskId() {
        return "Vm-Start:" + guestId + ":" + hostId;
    }
}
