package cn.roamblue.cloud.management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @author chenjun
 */
@Configuration
public class TimezoneConfig {
    @Value("${time.zone:Asia/Shanghai}")
    private String timeZone;

    @PostConstruct
    public void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }
}
