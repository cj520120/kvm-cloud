package cn.roamblue.cloud.management.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractTask implements CommandLineRunner {
    @Autowired
    @Qualifier("bossExecutorService")
    private ScheduledExecutorService bossExecutor;

    @Override
    public void run(String... args) throws Exception {
        this.bossExecutor.scheduleAtFixedRate(this::call, 10, 10, TimeUnit.SECONDS);
    }

    private void call() {
        try {
            this.dispatch();
        } catch (Exception err) {
            log.error("周期任务执行失败.", err);
        }
    }

    protected int getDelaySeconds() {
        return 10;
    }

    protected int getPeriodSeconds() {
        return 10;
    }

    /**
     * 任务分发
     *
     * @throws Exception
     */
    protected abstract void dispatch() throws Exception;
}
