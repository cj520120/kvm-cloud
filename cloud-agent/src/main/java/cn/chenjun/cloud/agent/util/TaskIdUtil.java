package cn.chenjun.cloud.agent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenjun
 */
public class TaskIdUtil {
    private static final ConcurrentHashMap<String, Long> TASK_LIST = new ConcurrentHashMap<>();

    public static void push(String taskId) {
        TaskIdUtil.TASK_LIST.put(taskId, System.currentTimeMillis());
    }

    public static void remove(String taskId) {
        TaskIdUtil.TASK_LIST.remove(taskId);
    }

    public static List<String> getTaskList() {
        return new ArrayList<>(TaskIdUtil.TASK_LIST.keySet());
    }
}
