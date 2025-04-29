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
public class CreateGuestOperate extends CreateVolumeOperate {
    private int guestId;
    private int hostId;
    private boolean start;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_GUEST;
    }

    @Override
    public String getId() {
        return "Vm-Create:" + guestId;
    }
}
