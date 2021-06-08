package com.roamblue.cloud.management.config;


import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
public class ThreadPoolConfig {
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService executorService(@Value("${async.thread.size:16}") int size) {
        return new ScheduledThreadPoolExecutor(size, new BasicThreadFactory.Builder().namingPattern("executor-pool-%d").daemon(true).build());
    }
}
