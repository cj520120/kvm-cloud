package cn.roamblue.cloud.management.operate.bean;

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
public class VolumeCheckOperate extends BaseOperateParam {
    private int storageId;
}
