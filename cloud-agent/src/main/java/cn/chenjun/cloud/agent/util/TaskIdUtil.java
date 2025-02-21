package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenjun
 */
public class TaskIdUtil {
    private static final int TASK_MAX_WAIT_SIZE = 100;
    private static final ConcurrentHashMap<String, Long> TASK_LIST = new ConcurrentHashMap<>();

    public static void push(String taskId) {
        if (TASK_LIST.size() >= TASK_MAX_WAIT_SIZE) {
            throw new CodeException(ErrorCode.BASE_TASK_BUSY, "任务达到最大数");
        }
        TaskIdUtil.TASK_LIST.put(taskId, System.currentTimeMillis());
    }

    public static int size() {
        return TASK_LIST.size();
    }
    public static void remove(String taskId) {
        TaskIdUtil.TASK_LIST.remove(taskId);
    }

    public static List<String> getTaskList() {
        return new ArrayList<>(TaskIdUtil.TASK_LIST.keySet());
    }
}
