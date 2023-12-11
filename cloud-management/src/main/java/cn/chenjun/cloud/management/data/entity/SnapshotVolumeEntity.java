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
@TableName("tbl_snapshot_volume")
public class SnapshotVolumeEntity {
    public static final String SNAPSHOT_VOLUME_ID = "snapshot_volume_id";
    public static final String SNAPSHOT_NAME = "snapshot_name";
    public static final String STORAGE_ID = "storage_id";
    public static final String VOLUME_NAME = "volume_name";
    public static final String VOLUME_PATH = "volume_path";
    public static final String VOLUME_CAPACITY = "volume_capacity";
    public static final String VOLUME_ALLOCATION = "volume_allocation";
    public static final String VOLUME_TYPE = "volume_type";
    public static final String VOLUME_STATUS = "volume_status";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO)
    @TableField(SNAPSHOT_VOLUME_ID)
    private Integer snapshotVolumeId;
    @TableField(SNAPSHOT_NAME)
    private String name;
    @TableField(STORAGE_ID)
    private Integer storageId;
    @TableField(VOLUME_NAME)
    private String volumeName;
    @TableField(VOLUME_PATH)
    private String volumePath;
    @TableField(VOLUME_CAPACITY)
    private Long capacity;
    @TableField(VOLUME_ALLOCATION)
    private Long allocation;
    @TableField(VOLUME_TYPE)
    private String type;
    @TableField(VOLUME_STATUS)
    private Integer status;
    @TableField(CREATE_TIME)
    private Date createTime;

}