package cn.roamblue.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeGuestNetworkInterfaceOperate extends BaseOperateParam {
    private int guestNetworkId;
    private int guestId;
    private boolean attach;
}
