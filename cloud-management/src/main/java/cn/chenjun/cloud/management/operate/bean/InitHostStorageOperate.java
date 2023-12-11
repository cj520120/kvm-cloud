package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.util.Constant;
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
public class InitHostStorageOperate extends BaseOperateParam {
    private int storageId;
    private List<Integer> nextHostIds;

    @Override
    public String toString() {
        return GsonBuilderUtil.create().toJson(this);
    }

    @Override
    public int getType() {
        return Constant.OperateType.INIT_HOST_STORAGE;
    }
}
