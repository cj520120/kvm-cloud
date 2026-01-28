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
@TableName("tbl_guest_network")
public class GuestNetworkEntity {
    public static final String GUEST_NETWORK_ID = "guest_network_id";
    public static final String ALLOCATE_ID = "allocate_id";
    public static final String ALLOCATE_TYPE = "allocate_type";
    public static final String ALLOCATE_DESCRIPTION = "allocate_description";
    public static final String NETWORK_ID = "network_id";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_TYPE = "device_type";
    public static final String NETWORK_MAC_ADDRESS = "network_mac_address";
    public static final String NETWORK_IP = "network_ip";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = GUEST_NETWORK_ID)
    private Integer guestNetworkId;
    @TableField(ALLOCATE_ID)
    private Integer allocateId;
    @TableField(ALLOCATE_TYPE)
    private Integer allocateType;
    @TableField(ALLOCATE_DESCRIPTION)
    private String allocateDescription;
    @TableField(NETWORK_ID)
    private Integer networkId;
    @TableField(DEVICE_ID)
    private Integer deviceId;
    @TableField(DEVICE_TYPE)
    private String driveType;
    @TableField(NETWORK_MAC_ADDRESS)
    private String mac;
    @TableField(NETWORK_IP)
    private String ip;
    @TableField(CREATE_TIME)
    private Date createTime;

}