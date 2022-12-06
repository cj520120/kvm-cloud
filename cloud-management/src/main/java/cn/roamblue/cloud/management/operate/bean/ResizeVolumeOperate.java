package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResizeVolumeOperate extends BaseOperateParam {
    private int volumeId;
    private long size;
}
