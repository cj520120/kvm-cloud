package cn.roamblue.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("tbl_vm_info")
public class VmEntity {

    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    @TableField("cluster_id")
    private Integer clusterId;
    @TableField("vm_name")
    private String vmName;
    @TableField("vm_description")
    private String vmDescription;
    @TableField("host_id")
    private Integer hostId;
    @TableField("calculation_scheme_id")
    private Integer calculationSchemeId;
    @TableField("vm_iso")
    private Integer vmIso;
    @TableField("template_id")
    private Integer templateId;
    @TableField("vm_ip")
    private String vmIp;
    @TableField("os_category_id")
    private Integer osCategoryId;
    @TableField("group_id")
    private Integer groupId;
    @TableField("vnc_port")
    private Integer vncPort;
    @TableField("vnc_password")
    private String vncPassword;
    @TableField("vm_type")
    private String vmType;
    @TableField("vm_status")
    private String vmStatus;
    @TableField("last_update_time")
    private Date lastUpdateTime;
    @TableField("create_time")
    private Date createTime;
    @TableField(value = "remove_time", updateStrategy = FieldStrategy.IGNORED, insertStrategy = FieldStrategy.IGNORED)
    private Date removeTime;

}
