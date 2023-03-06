package cn.chenjun.cloud.agent.config;

import cn.chenjun.cloud.agent.util.NetworkType;
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
    private int taskThreadSize;
    private String networkType= NetworkType.BRIDGE;
}
