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
@TableName("tbl_template_ref_info")
public class TemplateRefEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("storage_id")
    private Integer storageId;
    @TableField("template_id")
    private Integer templateId;
    @TableField("template_status")
    private String templateStatus;
    @TableField("template_target")
    private String templateTarget;
    @TableField("create_time")
    private Date createTime;

}
