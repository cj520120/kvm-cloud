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
public class StopGuestOperate extends BaseOperateParam {
    private int guestId;
    private boolean force;
    private boolean destroy;
}
