package cn.roamblue.cloud.management.v2.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeGuestCdRoomOperate extends BaseOperateInfo {
    private int id;
}
