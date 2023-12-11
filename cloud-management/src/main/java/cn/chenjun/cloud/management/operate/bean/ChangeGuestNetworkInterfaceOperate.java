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
public class ChangeGuestNetworkInterfaceOperate extends BaseOperateParam {
    private int guestNetworkId;
    private int guestId;
    private boolean attach;

    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_NETWORK_INTERFACE;
    }
}
