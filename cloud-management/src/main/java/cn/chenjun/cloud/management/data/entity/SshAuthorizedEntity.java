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
@TableName("tbl_ssh_authorized_keys")
public class SshAuthorizedEntity {
    public static final String SSH_AUTHORIZED_ID = "id";
    public static final String SSH_NAME = "ssh_name";
    public static final String SSH_PUBLIC_KEY = "ssh_public_key";
    public static final String SSH_PRIVATE_KEY = "ssh_private_key";

    @TableId(type = IdType.AUTO)
    @TableField(SSH_AUTHORIZED_ID)
    private Integer id;
    @TableField(SSH_NAME)
    private String sshName;
    @TableField(SSH_PUBLIC_KEY)
    private String sshPublicKey;
    @TableField(SSH_PRIVATE_KEY)
    private String sshPrivateKey;
}
