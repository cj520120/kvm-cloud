package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.util.Constant;
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
public class CreateGuestVolumeOperate extends CreateVolumeOperate {
    private int guestId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_GUEST_VOLUME;
    }

    @Override
    public String getId() {
        return "Guest-Vol-Create:" + guestId + ":" + this.getVolumeId();
    }
}
