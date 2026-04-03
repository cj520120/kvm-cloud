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
    private ClusterConfig cluster;

    @Data
    public static class ClusterConfig {
        private String token;
        private String nodeUrl;
    }
}
