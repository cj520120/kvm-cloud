package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("tbl_volume_info")
public class VolumeEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("storage_id")
    private Integer storageId;
    @TableField("vm_id")
    private Integer vmId;
    @TableField("vm_device")
    private Integer vmDevice;
    @TableField("volume_target")
    private String volumeTarget;
    @TableField("volume_name")
    private String volumeName;
    @TableField("volume_status")
    private String volumeStatus;
    @TableField("volume_capacity")
    private Long volumeCapacity;
    @TableField("volume_allocation")
    private Long volumeAllocation;
    @TableField(value = "remove_time", updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED)
    private Date removeTime;
    @TableField("create_time")
    private Date createTime;

}
