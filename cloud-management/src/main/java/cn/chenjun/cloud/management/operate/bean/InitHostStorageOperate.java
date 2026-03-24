package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
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
public class InitHostStorageOperate extends BaseOperateParam {
    private int storageId;
    private List<InitHostStorageOperate.HostStorageBind> hostStorageBinds;

    @Override
    public String toString() {
        return GsonBuilderUtil.create().toJson(this);
    }

    @Override
    public int getType() {
        return Constant.OperateType.INIT_HOST_STORAGE;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HostStorageBind {
        private Integer storageId;
        private Integer hostId;
    }
}
