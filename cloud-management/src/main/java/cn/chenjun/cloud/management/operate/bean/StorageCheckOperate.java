package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.management.util.Constant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
public class StorageCheckOperate extends BaseOperateParam {

    private int storageId;

    @Override
    public int getType() {
        return Constant.OperateType.STORAGE_CHECK;
    }

    @Override
    public String getTaskId() {
        return "STORAGE_CHECK:" + this.storageId;
    }
}
