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
@TableName("tbl_guest_info")
public class GuestEntity {
    public static final String GUEST_ID = "guest_id";
    public static final String GROUP_ID = "group_id";
    public static final String GUEST_NAME = "guest_name";
    public static final String GUEST_DESCRIPTION = "guest_description";
    public static final String GUEST_BUS_TYPE = "guest_bus_type";
    public static final String GUEST_CPU = "guest_cpu";
    public static final String GUEST_CPU_SPEED = "guest_cpu_speed";
    public static final String GUEST_MEMORY = "guest_memory";
    public static final String GUEST_CD_ROOM = "guest_cd_room";
    public static final String HOST_ID = "host_id";
    public static final String LAST_HOST_ID = "last_host_id";
    public static final String SCHEME_ID = "scheme_id";
    public static final String NETWORK_ID = "network_id";
    public static final String GUEST_IP = "guest_ip";
    public static final String OTHER_ID = "other_id";
    public static final String GUEST_TYPE = "guest_type";
    public static final String GUEST_STATUS = "guest_status";
    public static final String SYSTEM_CATEGORY = "system_category";
    public static final String LAST_START_TIME = "last_start_time";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO)
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(GROUP_ID)
    private Integer groupId;
    @TableField(GUEST_NAME)
    private String name;
    @TableField(GUEST_DESCRIPTION)
    private String description;
    @TableField(GUEST_BUS_TYPE)
    private String busType;
    @TableField(GUEST_CPU)
    private Integer cpu;
    @TableField(GUEST_CPU_SPEED)
    private Integer speed;
    @TableField(GUEST_MEMORY)
    private Long memory;
    @TableField(GUEST_CD_ROOM)
    private Integer cdRoom;
    @TableField(HOST_ID)
    private Integer hostId;
    @TableField(LAST_HOST_ID)
    private Integer lastHostId;
    @TableField(SCHEME_ID)
    private Integer schemeId;
    @TableField(NETWORK_ID)
    private Integer networkId;
    @TableField(GUEST_IP)
    private String guestIp;
    @TableField(OTHER_ID)
    private Integer otherId;
    @TableField(SYSTEM_CATEGORY)
    private Integer systemCategory;
    @TableField(GUEST_TYPE)
    private Integer type;
    @TableField(GUEST_STATUS)
    private Integer status;
    @TableField(LAST_START_TIME)
    private Date lastStartTime;
    @TableField(CREATE_TIME)
    private Date createTime;

}