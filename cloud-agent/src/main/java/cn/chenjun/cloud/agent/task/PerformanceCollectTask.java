package cn.chenjun.cloud.agent.task;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.util.TaskPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author chenjun
 */
@Configuration
@Slf4j
public class PerformanceCollectTask {
    @Autowired
    private ApplicationConfig applicationConfig;
    @Scheduled(fixedDelay = 2000)
    public void run() {
        int size=TaskPoolUtil.size();
        log.info("任务线程数:{} 等待的任务数目：{}",applicationConfig.getTaskThreadSize(), size);
    }
}
