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
public class MigrateGuestOperate extends BaseOperateParam {
    private int guestId;
    private int sourceHostId;
    private int toHostId;

    @Override
    public int getType() {
        return Constant.OperateType.MIGRATE_GUEST;
    }
}
