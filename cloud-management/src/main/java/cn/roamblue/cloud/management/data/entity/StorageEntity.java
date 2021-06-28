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
@TableName("tbl_storage_info")
public class StorageEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("storage_name")
    private String storageName;
    @TableField("storage_host")
    private String storageHost;
    @TableField("storage_source")
    private String storageSource;
    @TableField("storage_target")
    private String storageTarget;
    @TableField("storage_status")
    private String storageStatus;
    @TableField("storage_capacity")
    private Long storageCapacity;
    @TableField("storage_available")
    private Long storageAvailable;
    @TableField("storage_allocation")
    private Long storageAllocation;
    @TableField("create_time")
    private Date createTime;
}
