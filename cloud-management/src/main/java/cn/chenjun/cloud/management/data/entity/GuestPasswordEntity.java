package cn.chenjun.cloud.management.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("tbl_guest_password")
public class GuestPasswordEntity {
    @TableId(type = IdType.INPUT)
    @TableField("guest_id")
    private Integer guestId;
    @TableField("encode_key")
    private String encodeKey;
    @TableField("iv_key")
    private String ivKey;
    @TableField("guest_password")
    private String password;
}
