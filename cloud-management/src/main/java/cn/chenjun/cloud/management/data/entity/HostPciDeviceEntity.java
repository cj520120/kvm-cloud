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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_host_pci_device")
public class HostPciDeviceEntity {
    public static final String ID = "id";
    public static final String GUEST_ID = "guest_id";
    public static final String HOST_ID = "host_id";
    public static final String DOMAIN = "pci_domain";
    public static final String BUS = "pci_bus";
    public static final String SLOT = "pci_slot";
    public static final String FUNCTION = "pci_function";
    public static final String DESCRIPTION = "pci_description";
    public static final String CREATE_TIME = "create_time";
    public static final String UPDATE_TIME = "update_time";
    @TableId(type = IdType.AUTO, value = ID)
    private Integer id;
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(HOST_ID)
    private Integer hostId;
    @TableField(DOMAIN)
    private String domain;
    @TableField(BUS)
    private String bus;
    @TableField(SLOT)
    private String slot;
    @TableField(FUNCTION)
    private String func;
    @TableField(DESCRIPTION)
    private String description;
    @TableField(CREATE_TIME)
    private Date createTime;
    @TableField(UPDATE_TIME)
    private Date updateTime;
}
