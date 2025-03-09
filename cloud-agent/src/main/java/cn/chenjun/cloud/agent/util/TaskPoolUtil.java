package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.agent.operate.bean.DispatchProcess;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
public class TaskPoolUtil {
    private static final HashMap<String, Long> TASK_MAP = new HashMap<>();
    private static final LinkedBlockingQueue<DispatchProcess> TASK_QUEUE = new LinkedBlockingQueue<>();
    private static final Object SYNC_OBJECT = new Object();

    @SneakyThrows
    public static void push(DispatchProcess dispatch) {
        synchronized (SYNC_OBJECT) {
            try {
                if (!TaskPoolUtil.TASK_MAP.containsKey(dispatch.getTask().getTaskId())) {
                    TaskPoolUtil.TASK_QUEUE.put(dispatch);
                    TaskPoolUtil.TASK_MAP.put(dispatch.getTask().getTaskId(), System.currentTimeMillis());
                }
            } catch (Exception err) {
                log.error("加入队列失败.", err);
            }
        }
    }

    public static DispatchProcess offer() {
        if (TaskPoolUtil.TASK_QUEUE.isEmpty()) {
            return null;
        }
        try {
            return TaskPoolUtil.TASK_QUEUE.poll(1, TimeUnit.MILLISECONDS);
        } catch (Exception err) {
            log.error("弹出队列失败.", err);
            return null;
        }
    }

    public static int size() {
        return TASK_QUEUE.size();
    }

    public static void remove(String taskId) {
        synchronized (SYNC_OBJECT) {
            TaskPoolUtil.TASK_MAP.remove(taskId);
        }
    }

    public static List<String> getTaskMap() {
        synchronized (SYNC_OBJECT) {
            return new ArrayList<>(TaskPoolUtil.TASK_MAP.keySet());
        }
    }
}
