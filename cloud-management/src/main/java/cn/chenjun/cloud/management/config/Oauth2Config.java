package cn.chenjun.cloud.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author chenjun
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2")
@Component
public class Oauth2Config {
    private String title = "CJ Oauth2 Login";
    private boolean enable = false;
    private String clientId = "clientId";
    private String clientSecret = "clientSecret";
    private String authUri = "http://127.0.0.1:8081/oauth2/authorize";
    private String tokenUri = "http://127.0.0.1:8081/oauth2/token";
    private String userUri = "http://127.0.0.1:8081/oauth2/user/info";
    private String redirectUri = "http://127.0.0.1:8080/#/login";
    private List<String> idPath = Collections.singletonList("userId");
    private List<String> authoritiesPath = Collections.singletonList("authorities");
}
