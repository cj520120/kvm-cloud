package cn.roamblue.cloud.management.config;


import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
public class ThreadPoolConfig {
    @Bean(destroyMethod = "shutdown", name = "workExecutorService")
    @Primary
    public ScheduledExecutorService workExecutorService(@Value("${cloud.work.thread.size:8}") int size) {
        return new ScheduledThreadPoolExecutor(size, new BasicThreadFactory.Builder().namingPattern("executor-pool-%d").daemon(true).build());
    }

    @Bean(destroyMethod = "shutdown", name = "bossExecutorService")
    public ScheduledExecutorService bossExecutorService(@Value("${cloud.boss.thread.size:2}") int size) {
        return new ScheduledThreadPoolExecutor(size, new BasicThreadFactory.Builder().namingPattern("executor-pool-%d").daemon(true).build());
    }
}
