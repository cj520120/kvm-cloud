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
public class ChangeGuestPicOperate extends BaseOperateParam {
    private int guestId;
    private String domain;
    private String bus;
    private String slot;
    private String function;
    private boolean isAttach;


    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_PIC;
    }

    @Override
    public String getTaskId() {
        return "Vm-Pic:" + guestId + ":" + domain + ":" + bus + ":" + slot + ":" + function + ":" + isAttach;
    }
}
