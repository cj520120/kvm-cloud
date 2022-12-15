package cn.roamblue.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VncUpdateOperate extends BaseOperateParam {
    private int guestId;
    private String token;
    private String ip;
    private int port;
}
