package cn.chenjun.cloud.management.config;


import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
public class ThreadPoolConfig {
    @Bean(destroyMethod = "shutdown", name = "taskExecutorService")
    @Primary
    public ScheduledThreadPoolExecutor taskExecutorService(@Value("${app.task.thread.size:1}") int size) {
        return new ScheduledThreadPoolExecutor(Math.max(size, 1), new BasicThreadFactory.Builder().namingPattern("executor-pool-%d").daemon(true).build());
    }

    @Bean(destroyMethod = "shutdown", name = "workExecutorService")
    @Primary
    public ScheduledThreadPoolExecutor workExecutorService(@Value("${app.work.thread.size:1}") int size) {
        return new ScheduledThreadPoolExecutor(Math.max(size, 1), new BasicThreadFactory.Builder().namingPattern("executor-pool-%d").daemon(true).build());
    }
}
