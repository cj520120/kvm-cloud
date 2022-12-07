package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeGuestNetworkInterfaceOperate extends BaseOperateParam {
    private int guestNetworkId;
    private int guestId;
    private boolean attach;
}
