package cn.chenjun.cloud.management.data.entity;

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
    public static final String GUEST_DISK_ID = "guest_disk_id";
    public static final String GUEST_ID = "guest_id";
    public static final String VOLUME_ID = "volume_id";
    public static final String DEVICE_ID = "device_id";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO)
    @TableField(GUEST_DISK_ID)
    private Integer guestDiskId;
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(VOLUME_ID)
    private Integer volumeId;
    @TableField(DEVICE_ID)
    private Integer deviceId;
    @TableField(CREATE_TIME)
    private Date createTime;

}