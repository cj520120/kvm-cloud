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
public class ChangeGuestNetworkInterfaceOperate extends BaseOperateParam {
    private int guestNetworkId;
    private int guestId;
    private boolean attach;

    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_NETWORK_INTERFACE;
    }

    @Override
    public String getId() {
        return "Vm-Nic:" + guestId + ":" + guestNetworkId + ":" + attach;
    }
}
