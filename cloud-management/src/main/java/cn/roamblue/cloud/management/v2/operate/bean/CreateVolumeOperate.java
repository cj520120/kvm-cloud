package cn.roamblue.cloud.management.v2.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateVolumeOperate extends BaseOperateInfo {
    private int volumeId;
}
