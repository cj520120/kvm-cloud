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
@TableName("tbl_component_guest_info")
public class ComponentGuestEntity {
    public static final String COMPONENT_GUEST_ID = "component_guest_id";
    public static final String COMPONENT_ID = "component_id";
    public static final String COMPONENT_TYPE = "component_type";
    public static final String GUEST_ID = "guest_id";
    public static final String HOST_ID = "host_id";
    public static final String COMPONENT_STATUS = "component_status";
    public static final String COMPONENT_VERSION = "component_version";
    public static final String COMPONENT_START_TIME = "guest_start_time";
    public static final String ERROR_COUNT = "error_count";
    public static final String CREATE_TIME = "create_time";
    public static final String LAST_ACTIVE_TIME = "last_active_time";
    public static final String SESSION_ID = "session_id";
    @TableId(type = IdType.AUTO, value = COMPONENT_GUEST_ID)
    private Integer componentGuestId;
    @TableField(COMPONENT_ID)
    private Integer componentId;
    @TableField(COMPONENT_TYPE)
    private Integer componentType;
    @TableField(COMPONENT_VERSION)
    private String componentVersion;
    @TableField(HOST_ID)
    private Integer hostId;
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(SESSION_ID)
    private String sessionId;
    @TableField(COMPONENT_STATUS)
    private Integer status;
    @TableField(ERROR_COUNT)
    private int errorCount;
    @TableField(CREATE_TIME)
    private Date createTime;
    @TableField(LAST_ACTIVE_TIME)
    private Date lastActiveTime;

}
