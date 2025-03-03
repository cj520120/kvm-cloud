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
@TableName("tbl_template_volume")
public class TemplateVolumeEntity {
    public static final String TEMPLATE_VOLUME_ID = "template_volume_id";
    public static final String TEMPLATE_ID = "template_id";
    public static final String STORAGE_ID = "storage_id";
    public static final String TEMPLATE_NAME = "template_name";
    public static final String TEMPLATE_PATH = "template_path";
    public static final String TEMPLATE_CAPACITY = "template_capacity";
    public static final String TEMPLATE_ALLOCATION = "template_allocation";
    public static final String TEMPLATE_TYPE = "template_type";
    public static final String TEMPLATE_STATUS = "template_status";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO,value = TEMPLATE_VOLUME_ID)
    private Integer templateVolumeId;
    @TableField(TEMPLATE_ID)
    private Integer templateId;
    @TableField(STORAGE_ID)
    private Integer storageId;
    @TableField(TEMPLATE_NAME)
    private String name;
    @TableField(TEMPLATE_PATH)
    private String path;
    @TableField(TEMPLATE_CAPACITY)
    private Long capacity;
    @TableField(TEMPLATE_ALLOCATION)
    private Long allocation;
    @TableField(TEMPLATE_TYPE)
    private String type;
    @TableField(TEMPLATE_STATUS)
    private Integer status;
    @TableField(CREATE_TIME)
    private Date createTime;
}