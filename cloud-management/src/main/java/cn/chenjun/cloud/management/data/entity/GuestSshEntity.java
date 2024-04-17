package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_ssh_info")
public class GuestSshEntity {
    public static final String ID = "id";
    public static final String GUEST_ID = "guest_id";
    public static final String SSH_ID = "ssh_id";

    @TableId(type = IdType.AUTO)
    @TableField(ID)
    private Integer id;
    @TableField(GUEST_ID)
    private Integer guestId;
    @TableField(SSH_ID)
    private Integer sshId;
}
