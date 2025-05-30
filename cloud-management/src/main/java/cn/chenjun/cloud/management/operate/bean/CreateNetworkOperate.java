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
public class CreateNetworkOperate extends BaseOperateParam {
    private int networkId;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_NETWORK;
    }

    @Override
    public String getId() {
        return "Net-Create:" + networkId;
    }

}
