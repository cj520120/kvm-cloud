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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_template_volume")
public class TemplateVolumeEntity {
    @TableId(type = IdType.AUTO)
    @TableField("template_volume_id")
    private Integer templateVolumeId;
    @TableField("template_id")
    private int templateId;
    @TableField("storage_id")
    private int storageId;
    @TableField("template_name")
    private String name;
    @TableField("template_path")
    private String path;
    @TableField("template_capacity")
    private long capacity;
    @TableField("template_type")
    private String type;
    @TableField("template_status")
    private int status;
    @TableField("create_time")
    private Date createTime;
}
