package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InitHostNetworkOperate extends BaseOperateParam {
    private int networkId;
    private List<Integer> nextHostIds;

    @Override
    public int getType() {
        return Constant.OperateType.INIT_HOST_NETWORK;
    }
}
