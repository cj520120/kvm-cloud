package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractTask {

    @Autowired
    private ScheduledExecutorService executorService;
    @Autowired
    private ManagementService managementService;

    /**
     * 获取任务间隔时间毫秒
     *
     * @return
     */
    protected abstract int getInterval();

    /**
     * 获取任务名称
     *
     * @return
     */
    protected abstract String getName();

    @PostConstruct
    public void run() {
        executorService.scheduleWithFixedDelay(this::schedule, this.getInterval(), this.getInterval(), TimeUnit.MILLISECONDS);
    }

    public void schedule() {
        try {
            if (managementService.applyTask(this.getName())) {
                this.call();
            }
        } catch (Exception e) {
            log.error("task={} run fail.", this.getName(), e);
        }
    }

    /**
     * 执行任务
     */
    protected abstract void call();
}

