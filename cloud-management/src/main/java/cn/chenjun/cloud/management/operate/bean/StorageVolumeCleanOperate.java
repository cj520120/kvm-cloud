package cn.chenjun.cloud.management.operate.bean;

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
public class StorageVolumeCleanOperate extends BaseOperateParam {

    private int storageId;

    @Override
    public int getType() {
        return Constant.OperateType.STORAGE_VOLUME_CLEAR;
    }

    @Override
    public String getTaskId() {
        return "STORAGE_VOLUME_CLEAR:" + this.storageId;
    }
}
