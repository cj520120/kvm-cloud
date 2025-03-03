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
@TableName("tbl_guest_vnc")
public class GuestVncEntity {
    public static final String GUEST_ID = "guest_id";
    public static final String VNC_PORT = "vnc_port";
    public static final String VNC_PASSWORD = "vnc_password";
    public static final String VNC_TOKEN = "vnc_token";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.INPUT,value = GUEST_ID)
    private Integer guestId;
    @TableField(VNC_PORT)
    private Integer port;
    @TableField(VNC_PASSWORD)
    private String password;
    @TableField(VNC_TOKEN)
    private String token;
    @TableField(CREATE_TIME)
    private Date createTime;

}