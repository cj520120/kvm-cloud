package cn.chenjun.cloud.management.operate.bean;

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
public class ChangeGuestDiskOperate extends BaseOperateParam {
    private int deviceId;
    private int volumeId;
    private int guestId;
    private boolean attach;
}
