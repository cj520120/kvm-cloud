package cn.roamblue.cloud.management.v2.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_info")
public class GuestDiskEntity {
    private int id;
    private int guestId;
    private int volumeId;
    private int deviceId;

}
