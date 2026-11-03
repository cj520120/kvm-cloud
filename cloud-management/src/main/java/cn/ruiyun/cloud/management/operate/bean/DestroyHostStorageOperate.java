package cn.ruiyun.cloud.management.operate.bean;

import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
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
public class DestroyHostStorageOperate extends BaseOperateParam {
    private int storageId;
    private List<Integer> nextHostIds;


    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_HOST_STORAGE;
    }
}
