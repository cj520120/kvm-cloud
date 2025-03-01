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
public class HostCheckOperate extends BaseOperateParam {
    private int hostId;

    @Override
    public int getType() {
        return Constant.OperateType.HOST_CHECK;
    }

    @Override
    public String getTaskId() {
        return "HOST_CHECK:" + this.hostId;
    }
}
