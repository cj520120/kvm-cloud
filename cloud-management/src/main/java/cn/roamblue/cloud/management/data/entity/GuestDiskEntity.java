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

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_disk")
public class GuestDiskEntity {
    @TableId(type = IdType.AUTO)
    @TableField("guest_disk_id")
    private int guestDiskId;
    @TableField("guest_id")
    private int guestId;
    @TableField("volume_id")
    private int volumeId;
    @TableField("device_id")
    private int deviceId;
    @TableField("create_time")
    private Date createTime;

}
