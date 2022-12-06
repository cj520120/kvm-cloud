package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DownloadTemplateOperate extends BaseOperateParam {

    private int templateVolumeId;
}
