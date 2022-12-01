package cn.roamblue.cloud.agent.config;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(@Value("${job.thread.size:8}") int size) {
        return new ScheduledThreadPoolExecutor(size, new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
    }
}
