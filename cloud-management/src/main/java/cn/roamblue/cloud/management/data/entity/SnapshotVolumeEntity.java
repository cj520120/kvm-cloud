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
@TableName("tbl_snapshot_volume")
public class SnapshotVolumeEntity {

    @TableId(type = IdType.AUTO)
    @TableField("snapshot_volume_id")
    private Integer snapshotVolumeId;
    @TableField("snapshot_name")
    private String name;
    @TableField("storage_id")
    private int storageId;
    @TableField("volume_name")
    private String volumeName;
    @TableField("volume_path")
    private String volumePath;
    @TableField("volume_capacity")
    private long capacity;
    @TableField("volume_allocation")
    private long allocation;
    @TableField("volume_type")
    private String type;
    @TableField("volume_status")
    private int status;
    @TableField("create_time")
    private Date createTime;

}
