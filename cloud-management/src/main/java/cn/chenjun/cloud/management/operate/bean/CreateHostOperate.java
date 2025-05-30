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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateHostOperate extends BaseOperateParam {
    private int hostId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_HOST;
    }

    @Override
    public String getId() {
        return "Host-Create:" + hostId;
    }
}
