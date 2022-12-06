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
@TableName("tbl_template_info")
public class TemplateEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("template_name")
    private String templateName;
    @TableField("template_type")
    private String templateType;
    @TableField("template_status")
    private String templateStatus;
    @TableField("template_uri")
    private String templateUri;
    @TableField("os_category_id")
    private Integer osCategoryId;
    @TableField("template_size")
    private Long templateSize;
    @TableField("create_time")
    private Date createTime;

}
