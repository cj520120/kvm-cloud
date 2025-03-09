package cn.chenjun.cloud.management.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
@Slf4j
public class PerformanceCollectTask {
    @Autowired
    @Qualifier("taskExecutorService")
    private ScheduledThreadPoolExecutor taskPoolExecutor;
    @Autowired
    @Qualifier("workExecutorService")
    private ScheduledThreadPoolExecutor workPoolExecutor;

    @Scheduled(fixedDelay = 2000)
    public void run() {
        log.info("Task线程池中线程数目：{}，队列中等待执行的任务数目：{}，已执行完的任务数目：{}", taskPoolExecutor.getPoolSize(), taskPoolExecutor.getQueue().size(), taskPoolExecutor.getCompletedTaskCount());
        log.info("Work线程池中线程数目：{}，队列中等待执行的任务数目：{}，已执行完的任务数目：{}", workPoolExecutor.getPoolSize(), workPoolExecutor.getQueue().size(), workPoolExecutor.getCompletedTaskCount());
    }
}
