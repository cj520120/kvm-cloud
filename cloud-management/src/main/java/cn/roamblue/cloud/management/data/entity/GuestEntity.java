package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_info")
public class GuestEntity {
    @TableId(type = IdType.AUTO)
    @TableField("guest_id")
    private Integer guestId;
    @TableField("guest_name")
    private String name;
    @TableField("guest_description")
    private String description;
    @TableField("guest_bus_type")
    private String busType;
    @TableField("guest_cpu")
    private Integer cpu;
    @TableField("guest_memory")
    private Long memory;
    @TableField("guest_cd_room")
    private Integer cdRoom;
    @TableField("host_id")
    private Integer hostId;
    @TableField("last_host_id")
    private Integer lastHostId;
    @TableField("guest_type")
    private Integer type;
    @TableField("guest_status")
    private Integer status;
    @TableField("create_time")
    private Date createTime;

}
