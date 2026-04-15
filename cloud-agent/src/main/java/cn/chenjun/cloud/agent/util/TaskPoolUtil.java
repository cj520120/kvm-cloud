package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.agent.operate.bean.DispatchProcess;
import cn.chenjun.cloud.agent.operate.bean.SubmitTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
public class TaskPoolUtil {
    private static final HashMap<String, Long> TASK_MAP = new HashMap<>();
    private static final LinkedBlockingQueue<DispatchProcess> TASK_QUEUE = new LinkedBlockingQueue<>();

    private static final LinkedBlockingQueue<SubmitTask> SUBMIT_QUEUE = new LinkedBlockingQueue<>();
    private static final Object SYNC_OBJECT = new Object();

    @SneakyThrows
    public static void pushDispatch(DispatchProcess dispatch) {
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

    public static void pushSubmit(SubmitTask submitTask) {
        synchronized (SYNC_OBJECT) {
            try {
                TaskPoolUtil.SUBMIT_QUEUE.put(submitTask);
            } catch (Exception err) {
                log.error("加入队列失败.", err);
            }
        }
    }

    public static DispatchProcess offerDispatch() {

        try {
            return TaskPoolUtil.TASK_QUEUE.poll(10, TimeUnit.SECONDS);
        } catch (Exception err) {
            log.error("弹出队列失败.", err);
            return null;
        }
    }

    public static SubmitTask offerSubmit() {
        try {
            return TaskPoolUtil.SUBMIT_QUEUE.poll(10, TimeUnit.SECONDS);
        } catch (Exception err) {
            log.error("弹出队列失败.", err);
            return null;
        }
    }

    public static int size() {
        synchronized (SYNC_OBJECT) {
            return TASK_MAP.size() + TaskPoolUtil.SUBMIT_QUEUE.size();
        }
    }

    public static void removeDispatch(String taskId) {
        synchronized (SYNC_OBJECT) {
            TaskPoolUtil.TASK_MAP.remove(taskId);
        }
    }

    public static void removeSubmit(String taskId) {
        synchronized (SYNC_OBJECT) {
            TaskPoolUtil.SUBMIT_QUEUE.removeIf(submit -> taskId.equals(submit.getTaskId()));
        }
    }

    public static List<String> getAllTaskIds() {
        synchronized (SYNC_OBJECT) {
            ArrayList<String> list = new ArrayList<>();
            list.addAll(TaskPoolUtil.TASK_MAP.keySet());
            list.addAll(TaskPoolUtil.SUBMIT_QUEUE.stream().map(SubmitTask::getTaskId).collect(Collectors.toList()));
            return new ArrayList<>(TaskPoolUtil.TASK_MAP.keySet());
        }
    }
}
