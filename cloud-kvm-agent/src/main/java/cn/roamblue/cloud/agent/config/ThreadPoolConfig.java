package cn.roamblue.cloud.agent.config;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
public class ThreadPoolConfig {
    @Autowired
    private ApplicationConfig config;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(config.getTaskThreadSize(), new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
    }
}
