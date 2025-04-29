package cn.chenjun.cloud.management.model;

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

public class RefreshTokenModel {
    /**
     * 过期时间
     */
    private Date expire;
    /**
     * 用户信息
     */
    private UserInfoModel self;
}
