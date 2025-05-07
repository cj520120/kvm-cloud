package cn.chenjun.cloud.common.core.operate;

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
    private String id;
    private String title;

    /**
     * 获取类型
     *
     * @return
     */
    public abstract int getType();

    public String getTaskId() {
        return this.id;
    }

    @Override
    public String toString() {
        return GsonBuilderUtil.create().toJson(this);
    }

}
