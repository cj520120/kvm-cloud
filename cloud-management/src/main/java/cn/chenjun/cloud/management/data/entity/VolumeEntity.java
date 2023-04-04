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
@TableName("tbl_volume_info")
public class VolumeEntity {

    @TableId(type = IdType.AUTO)
    @TableField("volume_id")
    private Integer volumeId;
    @TableField("template_id")
    private Integer templateId;
    @TableField("storage_id")
    private Integer storageId;
    @TableField("volume_name")
    private String name;
    @TableField("volume_description")
    private String description;
    @TableField("volume_path")
    private String path;
    @TableField("volume_capacity")
    private Long capacity;
    @TableField("volume_allocation")
    private Long allocation;
    @TableField("volume_type")
    private String type;
    @TableField("volume_backing_path")
    private String backingPath;
    @TableField("volume_status")
    private Integer status;
    @TableField("create_time")
    private Date createTime;

}
