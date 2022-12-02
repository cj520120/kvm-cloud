package cn.roamblue.cloud.management.v2.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_info")
public class GuestNetworkEntity {
    private int id;
    private int guestId;
    private int networkId;
    private int deviceId;
    private String drive;
    private String mac;
    private String ip;

}
