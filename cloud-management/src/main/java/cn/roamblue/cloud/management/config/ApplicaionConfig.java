package cn.roamblue.cloud.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName: ApplicaionConfig
 * @Create by: chenjun
 * @Date: 2021/7/30 上午11:51
 */
@Data
@Component
@ConfigurationProperties(prefix = "roamblue.cloud")
public class ApplicaionConfig {
    private int stopTimeout=180;
}
