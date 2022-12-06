package cn.roamblue.cloud.management.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * @author chenjun
 */
@Configuration
public class TimezoneConfig {
    @Autowired
	private ApplicaionConfig config;

    @PostConstruct
    public void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(this.config.getTimeZone()));
    }
}
