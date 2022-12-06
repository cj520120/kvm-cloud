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
@TableName("tbl_template_info")
public class TemplateEntity {
    @TableId(type = IdType.AUTO)
    @TableField("template_id")
    private int templateId;
    @TableField("template_name")
    private String name;
    @TableField("template_uri")
    private String uri;
    @TableField("template_type")
    private int type;
    @TableField("template_status")
    private int status;
    @TableField("create_time")
    private Date createTime;
}
