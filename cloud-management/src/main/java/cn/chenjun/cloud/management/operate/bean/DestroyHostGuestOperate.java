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
public class DestroyHostGuestOperate extends BaseOperateParam {
    private String name;
    private int hostId;

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_HOST_GUEST;
    }
}
