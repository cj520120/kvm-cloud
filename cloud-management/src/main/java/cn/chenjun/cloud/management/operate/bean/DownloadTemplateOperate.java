package cn.chenjun.cloud.management.operate.bean;

import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.management.util.Constant;
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
public class DownloadTemplateOperate extends BaseOperateParam {

    private int templateVolumeId;

    @Override
    public int getType() {
        return Constant.OperateType.DOWNLOAD_TEMPLATE;
    }

    @Override
    public String getId() {
        return "Template-Download:" + templateVolumeId;
    }

}
