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
@TableName("tbl_user_info")
public class UserInfoEntity {
    public static final String USER_ID = "user_id";
    public static final String LOGIN_NAME = "login_name";
    public static final String LOGIN_PASSWORD = "login_password";
    public static final String LOGIN_STATE = "login_state";
    public static final String LOGIN_PASSWORD_SALT = "login_password_salt";
    public static final String CREATE_TIME = "create_time";

    @TableId(type = IdType.AUTO, value = USER_ID)
    private Integer userId;

    /**
     * 用户名
     */
    @TableField(LOGIN_NAME)
    private String loginName;
    /**
     * 密码
     */
    @TableField(LOGIN_PASSWORD)
    private String loginPassword;
    /**
     *
     */
    @TableField(LOGIN_STATE)
    private Short loginState;
    /**
     * 密码
     */
    @TableField(LOGIN_PASSWORD_SALT)
    private String loginPasswordSalt;

    /**
     * 密码
     */
    @TableField(CREATE_TIME)
    private Date createTime;
}