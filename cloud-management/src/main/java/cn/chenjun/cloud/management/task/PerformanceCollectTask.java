package cn.chenjun.cloud.management.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Configuration
@Slf4j
public class  PerformanceCollectTask {
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Scheduled(fixedDelay = 2000)
    public void run() {
        log.info("线程池中线程数目：{}，队列中等待执行的任务数目：{}，已执行完的任务数目：{}", threadPoolExecutor.getPoolSize(), threadPoolExecutor.getQueue().size(), threadPoolExecutor.getCompletedTaskCount());
    }
}
