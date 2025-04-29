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
public class GuestInfoOperate extends BaseOperateParam {
    private int guestId;

    @Override
    public int getType() {
        return Constant.OperateType.GUEST_INFO;
    }

    @Override
    public String getId() {
        return "Vm-Info:" + guestId;
    }
}
