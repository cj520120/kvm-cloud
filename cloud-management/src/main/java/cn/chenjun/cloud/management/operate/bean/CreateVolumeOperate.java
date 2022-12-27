package cn.chenjun.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateVolumeOperate extends BaseOperateParam {
    private int volumeId;
    private int templateId;
    private int snapshotVolumeId;
}
