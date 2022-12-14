package cn.roamblue.cloud.management.operate.bean;

import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode
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
}
