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
@TableName("tbl_volume_info")
public class VolumeEntity {

    @TableId(type = IdType.AUTO)
    @TableField("volume_id")
    private Integer volumeId;
    @TableField("template_id")
    private int templateId;
    @TableField("storage_id")
    private int storageId;
    @TableField("volume_name")
    private String name;
    @TableField("volume_path")
    private String path;
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
