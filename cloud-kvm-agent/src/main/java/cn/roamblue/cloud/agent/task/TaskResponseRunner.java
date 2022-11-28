package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.common.bean.TaskResponse;
import cn.roamblue.cloud.common.task.ScheduledExecutor;

/**
 * @author chenjun
 */
public class TaskResponseRunner<R> implements ScheduledExecutor.ScheduleRunner<TaskResponse<R>, Void> {
    @Override
    public Void run(TaskResponse<R> param) throws Throwable {
        return null;
    }

    @Override
    public void onScheduleFinish(boolean isSuccess, TaskResponse<R> param, Void result, Throwable err) {

    }
}
