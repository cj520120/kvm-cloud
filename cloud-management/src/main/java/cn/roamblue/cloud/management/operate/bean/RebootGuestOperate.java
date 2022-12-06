package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebootGuestOperate extends BaseOperateParam {
    private int guestId;
}
