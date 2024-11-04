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
@TableName("tbl_template_info")
public class TemplateEntity {
    public static final String TEMPLATE_ID = "template_id";
    public static final String TEMPLATE_NAME = "template_name";
    public static final String TEMPLATE_URI = "template_uri";
    public static final String TEMPLATE_TYPE = "template_type";
    public static final String TEMPLATE_VOLUME_TYPE = "template_volume_type";
    public static final String TEMPLATE_STATUS = "template_status";
    public static final String TEMPLATE_MD5 = "template_md5";
    public static final String TEMPLATE_INIT_SCRIPT = "template_cloud_init_script";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO)
    @TableField(TEMPLATE_ID)
    private Integer templateId;
    @TableField(TEMPLATE_NAME)
    private String name;
    @TableField(TEMPLATE_URI)
    private String uri;
    @TableField(TEMPLATE_TYPE)
    private Integer templateType;
    @TableField(TEMPLATE_VOLUME_TYPE)
    private String volumeType;
    @TableField(TEMPLATE_STATUS)
    private Integer status;
    @TableField(TEMPLATE_MD5)
    private String md5;
    @TableField(TEMPLATE_INIT_SCRIPT)
    private String script;
    @TableField(CREATE_TIME)
    private Date createTime;
}