package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
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
public class DestroyTemplateOperate extends BaseOperateParam {
    private int templateId;

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_TEMPLATE;
    }

    @Override
    public String getId() {
        return "Template-Destroy:" + templateId;
    }
}
