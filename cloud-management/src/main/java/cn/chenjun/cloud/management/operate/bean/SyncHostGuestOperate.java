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
public class SyncHostGuestOperate extends BaseOperateParam {
    private int hostId;

    @Override
    public int getType() {
        return Constant.OperateType.SYNC_HOST_GUEST;
    }

    @Override
    public String getTaskId() {
        return "SYNC_HOST_GUEST:" + this.hostId;
    }
}
