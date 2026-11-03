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
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateNetworkOperate extends BaseOperateParam {
    private int networkId;
    private int networkType;

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_NETWORK;
    }

    @Override
    public String getTaskId() {
        return "Net-Create:" + networkId;
    }

}
