package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 登陆用户信息
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserInfo {

    /**
     * userId
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String loginName;
    /**
     * 密码
     */
    private String passwordSalt;
}