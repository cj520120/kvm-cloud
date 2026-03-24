package cn.chenjun.cloud.management.servcie.bean;

import cn.chenjun.cloud.management.data.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 登陆信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RefreshTokenInfo {
    /**
     * 过期时间
     */
    private Date expire;
    /**
     * 用户信息
     */
    private UserEntity self;
}
