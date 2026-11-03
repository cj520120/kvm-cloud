package cn.ruiyun.cloud.management.operate.bean;

import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
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
    public String getTaskId() {
        return "Template-Download:" + templateVolumeId;
    }

}
