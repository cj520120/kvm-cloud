package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeGuestDiskOperate extends BaseOperateParam {
    private int guestDiskId;
    private int volumeId;
    private int guestId;
    private boolean attach;
}
