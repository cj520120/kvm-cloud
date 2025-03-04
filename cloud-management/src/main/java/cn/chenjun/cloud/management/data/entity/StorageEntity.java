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
@TableName("tbl_storage_info")
public class StorageEntity {
    public static final String STORAGE_ID = "storage_id";
    public static final String STORAGE_DESCRIPTION = "storage_description";
    public static final String STORAGE_NAME = "storage_name";
    public static final String STORAGE_TYPE = "storage_type";
    public static final String STORAGE_HOST_ID = "storage_host_id";
    public static final String STORAGE_PARM = "storage_parm";
    public static final String STORAGE_SUPPORT_CATEGORY = "storage_support_category";
    public static final String STORAGE_MOUNT_PATH = "storage_mount_path";
    public static final String STORAGE_CAPACITY = "storage_capacity";
    public static final String STORAGE_AVAILABLE = "storage_available";
    public static final String STORAGE_ALLOCATION = "storage_allocation";
    public static final String STORAGE_STATUS = "storage_status";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = STORAGE_ID)
    private Integer storageId;
    @TableField(STORAGE_DESCRIPTION)
    private String description;
    @TableField(STORAGE_NAME)
    private String name;
    @TableField(STORAGE_TYPE)
    private String type;
    @TableField(STORAGE_HOST_ID)
    private Integer hostId;
    @TableField(STORAGE_PARM)
    private String param;
    @TableField(STORAGE_MOUNT_PATH)
    private String mountPath;
    @TableField(STORAGE_CAPACITY)
    private Long capacity;
    @TableField(STORAGE_AVAILABLE)
    private Long available;
    @TableField(STORAGE_ALLOCATION)
    private Long allocation;
    @TableField(STORAGE_SUPPORT_CATEGORY)
    private Integer supportCategory;
    @TableField(STORAGE_STATUS)
    private Integer status;
    @TableField(CREATE_TIME)
    private Date createTime;
}