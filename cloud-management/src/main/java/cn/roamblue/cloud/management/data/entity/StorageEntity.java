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
    @TableField("storage_id")
    private Integer storageId;
    @TableField("cluster_id")
    private int clusterId;
    @TableField("storage_name")
    private String name;
    @TableField("storage_type")
    private String type;
    @TableField("storage_parm")
    private String param;
    @TableField("storage_mount_path")
    private String mountPath;
    @TableField("storage_capacity")
    private long capacity;
    @TableField("storage_available")
    private long available;
    @TableField("storage_allocation")
    private long allocation;
    @TableField("storage_status")
    private int status;
    @TableField("create_time")
    private Date createTime;
}
