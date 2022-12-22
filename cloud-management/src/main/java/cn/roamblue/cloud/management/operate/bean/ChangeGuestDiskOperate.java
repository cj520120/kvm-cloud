package cn.roamblue.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
