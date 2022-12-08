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
@TableName("tbl_guest_network")
public class GuestNetworkEntity {
    @TableId(type = IdType.AUTO)
    @TableField("guest_network_id")
    private Integer guestNetworkId;
    @TableField("guest_id")
    private Integer guestId;
    @TableField("network_id")
    private Integer networkId;
    @TableField("device_id")
    private Integer deviceId;
    @TableField("device_type")
    private String driveType;
    @TableField("network_mac_address")
    private String mac;
    @TableField("network_ip")
    private String ip;
    @TableField("create_time")
    private Date createTime;

}
