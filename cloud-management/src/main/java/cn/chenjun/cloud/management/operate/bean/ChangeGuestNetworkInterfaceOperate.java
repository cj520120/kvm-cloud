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
public class ChangeGuestNetworkInterfaceOperate extends BaseOperateParam {
    private int guestNetworkId;
    private int guestId;
    private boolean attach;
}
