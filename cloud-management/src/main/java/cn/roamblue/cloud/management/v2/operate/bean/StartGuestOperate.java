package cn.roamblue.cloud.management.v2.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartGuestOperate extends BaseOperateInfo {
    private int id;
}
