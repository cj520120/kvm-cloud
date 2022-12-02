package cn.roamblue.cloud.management.v2.data.entity;

import cn.roamblue.cloud.common.bean.GuestStartRequest;
import cn.roamblue.cloud.common.bean.OsCdRoom;
import cn.roamblue.cloud.common.bean.OsCpu;
import cn.roamblue.cloud.common.bean.OsMemory;
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
public class GuestEntity {
    private int id;
    private int hostId;
    private String name;
    private String description;
    private String bus;
    private int cpu;
    private long memory;
    private String cdRoom;
    private int vncPort;
    private String vncPassword;
    private int status;

}
