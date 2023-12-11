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
public class ChangeGuestCdRoomOperate extends BaseOperateParam {
    private int guestId;


    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_CD_ROOM;
    }
}
