package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseOperateParam {
    private String taskId;
    private String title;

    /**
     * 获取类型
     *
     * @return
     */
    public abstract int getType();
    @Override
    public String toString() {
        return GsonBuilderUtil.create().toJson(this);
    }
}
