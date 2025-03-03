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
@TableName("tbl_guest_password")
public class GuestPasswordEntity {
    public static final String GUEST_ID = "guest_id";
    public static final String ENCODE_KEY = "encode_key";
    public static final String IV_KEY = "iv_key";
    public static final String GUEST_PASSWORD = "guest_password";

    @TableId(type = IdType.INPUT,value = GUEST_ID)
    private Integer guestId;
    @TableField(ENCODE_KEY)
    private String encodeKey;
    @TableField(IV_KEY)
    private String ivKey;
    @TableField(GUEST_PASSWORD)
    private String password;
}