package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.task.runner.AbstractRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TaskEngine implements CommandLineRunner {
    @Autowired
    private ScheduledExecutorService executor;
    @Autowired
    private List<AbstractRunner> runnerList;

    @Override
    public void run(String... args) throws Exception {
        for (AbstractRunner runner : runnerList) {
            this.executor.scheduleAtFixedRate(runner::call, runner.getDelaySeconds(), 1, TimeUnit.SECONDS);
        }
    }


}
