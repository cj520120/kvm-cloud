package cn.chenjun.cloud.agent.config;

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
        return new ScheduledThreadPoolExecutor(Math.max(config.getTaskThreadSize(), 1), new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
    }
}
