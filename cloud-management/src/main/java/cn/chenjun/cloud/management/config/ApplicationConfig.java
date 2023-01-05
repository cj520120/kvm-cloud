package cn.chenjun.cloud.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author chenjun
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig {
    private String managerUri;
    private float overCpu = 1.0f;
    private float overMemory = 1.0f;
    /**
     * JWT 密码
     */
    private String jwtPassword = "#$1fa)&*WS09";
    /**
     * ISSUser
     */
    private String jwtIssuer = "CJ Cloud Management";
}
