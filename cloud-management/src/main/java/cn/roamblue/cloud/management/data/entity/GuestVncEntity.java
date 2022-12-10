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
@TableName("tbl_guest_vnc")
public class GuestVncEntity {
    @TableId(type = IdType.INPUT)
    @TableField("guest_id")
    private Integer guestId;
    @TableField("vnc_port")
    private Integer port;
    @TableField("vnc_password")
    private String password;
    @TableField("vnc_token")
    private String token;
    @TableField("create_time")
    private Date createTime;

}
