package cn.ruiyun.cloud.management.servcie.bean;

import cn.ruiyun.cloud.management.data.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TokenInfo {
    /**
     * 用户Token
     */
    private String token;
    /**
     * 过期时间
     */
    private Date expire;
    /**
     * 用户信息
     */
    private UserEntity self;
}
