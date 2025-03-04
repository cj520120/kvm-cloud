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
@TableName("tbl_nat_info")
public class NatEntity {
    public static final String NAT_ID = "nat_id";
    public static final String COMPONENT_ID = "component_id";
    public static final String PROTOCOL = "nat_protocol";
    public static final String LOCAL_PORT = "nat_local_port";
    public static final String REMOTE_IP = "name_remote_ip";
    public static final String REMOTE_PORT = "name_remote_port";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = NAT_ID)
    private Integer natId;
    @TableField(COMPONENT_ID)
    private Integer componentId;
    @TableField(PROTOCOL)
    private String protocol;
    @TableField(LOCAL_PORT)
    private int localPort;

    @TableField(REMOTE_IP)
    private String remoteIp;
    @TableField(REMOTE_PORT)
    private int remotePort;
    @TableField(CREATE_TIME)
    private Date createTime;

}