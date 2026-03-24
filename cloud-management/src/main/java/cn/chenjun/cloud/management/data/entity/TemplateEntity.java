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
    public static final String TEMPLATE_STATUS = "template_status";
    public static final String TEMPLATE_MD5 = "template_md5";
    public static final String TEMPLATE_ARCH = "template_arch";
    public static final String TEMPLATE_VENDOR_DATA = "template_vendor_data";
    public static final String TEMPLATE_LOCAL_CLOUD_CFG = "template_local_cloud_cfg";
    public static final String CLOUD_WAIT_FLAG = "cloud_wait_flag";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = TEMPLATE_ID)
    private Integer templateId;
    @TableField(TEMPLATE_NAME)
    private String name;
    @TableField(TEMPLATE_URI)
    private String uri;
    @TableField(TEMPLATE_TYPE)
    private Integer templateType;
    @TableField(TEMPLATE_ARCH)
    private String arch;
    @TableField(TEMPLATE_STATUS)
    private Integer status;
    @TableField(TEMPLATE_MD5)
    private String md5;
    @TableField(CLOUD_WAIT_FLAG)
    private int cloudWaitFlag;
    @TableField(TEMPLATE_VENDOR_DATA)
    private String vendorData;
    @TableField(TEMPLATE_LOCAL_CLOUD_CFG)
    private String localCloudCfg;
    @TableField(CREATE_TIME)
    private Date createTime;
}