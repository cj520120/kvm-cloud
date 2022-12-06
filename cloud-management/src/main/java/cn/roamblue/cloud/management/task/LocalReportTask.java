package cn.roamblue.cloud.management.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class LocalReportTask implements CommandLineRunner {
    private final ConcurrentHashMap<String, Long> taskIdMap = new ConcurrentHashMap<>();
    @Autowired
    @Lazy
    private OperateTask operateTask;
    @Autowired
    @Qualifier("bossExecutorService")
    private ScheduledExecutorService bossExecutor;
    private final Object syncObject = new Object();

    public void addTaskId(String taskId) {
        synchronized (this.syncObject) {
            taskIdMap.put(taskId, System.currentTimeMillis());
        }
    }

    public void removeTaskId(String taskId) {
        synchronized (this.syncObject) {
            taskIdMap.remove(taskId);
        }
    }

    public void report() {
        List<String> taskIds;
        synchronized (this.syncObject) {
            taskIds = taskIdMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        }
        taskIds.stream().forEach(operateTask::keepTask);
    }

    @Override
    public void run(String... args) throws Exception {
        this.bossExecutor.scheduleAtFixedRate(this::report, 10, 20, TimeUnit.SECONDS);
    }
}
