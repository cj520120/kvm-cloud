package cn.chenjun.cloud.agent.config;

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
    private Machine machine;
    private UefiConfig uefi;
    private Cd cd;

    @Data
    public static class Machine {

        private String name;
        private String arch;
    }

    @Data
    public static class UefiConfig {

        private String type;
        private String path;
    }

    @Data
    public static class Cd {

        private String bus;
    }
}
