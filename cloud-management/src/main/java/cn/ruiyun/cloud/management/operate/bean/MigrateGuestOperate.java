package cn.ruiyun.cloud.management.operate.bean;

import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
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
public class MigrateGuestOperate extends BaseOperateParam {
    private int guestId;
    private int sourceHostId;
    private int toHostId;

    @Override
    public int getType() {
        return Constant.OperateType.MIGRATE_GUEST;
    }

    @Override
    public String getTaskId() {
        return "Vm-Migrate:" + guestId + ":" + sourceHostId + ":" + toHostId;
    }
}
