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
public class ChangeGuestDiskOperate extends BaseOperateParam {
    private int deviceId;
    private String deviceBus;
    private int volumeId;
    private int guestId;
    private boolean attach;

    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_DISK;
    }

    @Override
    public String getId() {
        return "Vm-Disk:" + guestId + ":" + volumeId + ":" + attach;
    }
}
