package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.operate.bean.DispatchProcess;
import cn.chenjun.cloud.agent.operate.bean.SubmitTask;
import cn.chenjun.cloud.agent.operate.process.DispatchFactory;
import cn.chenjun.cloud.agent.util.ConnectFactory;
import cn.chenjun.cloud.agent.util.TaskPoolUtil;
import cn.chenjun.cloud.agent.ws.SessionManager;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.TaskRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatch implements CommandLineRunner, Closeable {
    @Autowired
    private DispatchFactory dispatchFactory;
    @Autowired
    private SessionManager sessionManager;


    private ScheduledThreadPoolExecutor executor;

    @Autowired
    private ApplicationConfig applicationConfig;


    public <T, V> void dispatch(String data) {
        TaskRequest task = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
        Dispatch<T, V> dispatch = this.dispatchFactory.getDispatch(task.getCommand());
        if (dispatch == null) {
            ResultUtil<T> executeResult = ResultUtil.<T>builder().code(ErrorCode.NOT_SUPPORT_METHOD).message("不支持的操作:" + task.getCommand()).build();
            SubmitTask submitTask = SubmitTask.builder().taskId(task.getTaskId()).data(GsonBuilderUtil.create().toJson(executeResult)).build();
            TaskPoolUtil.pushSubmit(submitTask);
        }
        if (dispatch.isAsync()) {
            DispatchProcess dispatchProcess = DispatchProcess.builder().dispatch(dispatch).task(task).build();
            TaskPoolUtil.pushDispatch(dispatchProcess);
            log.info("提交异步任务:{}[{}]", task.getCommand(), task.getTaskId());
        } else {
            log.info("同步执行任务{}[{}]", task.getCommand(), task.getTaskId());
            ResultUtil<T> executeResult = dispatchTaskConsumer(task, dispatch);
            SubmitTask submitTask = SubmitTask.builder().taskId(task.getTaskId()).data(GsonBuilderUtil.create().toJson(executeResult)).build();
            TaskPoolUtil.pushSubmit(submitTask);
        }
    }

    private <T, V> ResultUtil<T> dispatchTaskConsumer(TaskRequest task, Dispatch<T, V> dispatch) {
        ResultUtil<T> executeResult = null;
        Connect connect = null;
        try {
            connect = ConnectFactory.create();
            log.info("开始执行任务:{}-{}",task.getCommand(),task.getTaskId());
            long startTime = System.currentTimeMillis();
            V param = StringUtils.isEmpty(task.getData()) ? null : GsonBuilderUtil.create().fromJson(task.getData(), dispatch.getParamType());
            Consumer<T, V> consumer = dispatch.getConsumer();
            T result = consumer.dispatch(connect, param);
            log.info("执行任务结束:{}-{} 耗时={}ms,返回:{}", task.getCommand(), task.getTaskId(), System.currentTimeMillis() - startTime, result);
            executeResult = ResultUtil.success(result);
        } catch (CodeException err) {
            log.warn("执行任务失败. {}-{} code={}", task.getCommand(), task.getTaskId(), err.getCode());
            executeResult = ResultUtil.error(err.getCode(), err.getMessage());
        } catch (Exception err) {
            log.error("执行任务失败.{}-{}", task.getCommand(), task.getTaskId(), err);
            executeResult = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        } finally {
            SubmitTask submitTask = SubmitTask.builder().taskId(task.getTaskId()).data(GsonBuilderUtil.create().toJson(executeResult)).build();
            TaskPoolUtil.pushSubmit(submitTask);
            TaskPoolUtil.removeDispatch(task.getTaskId());
            if (connect != null) {
                try {
                    connect.close();
                } catch (Exception e) {
                    log.error("关闭连接失败.", e);
                }
            }
        }
        return executeResult;
    }

    @Override
    public void run(String... args) throws Exception {
        int taskSize = Math.max(this.applicationConfig.getTaskThreadSize(), 1);
        this.executor = new ScheduledThreadPoolExecutor(taskSize, new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
        for (int i = 0; i < taskSize; i++) {
            this.executor.scheduleAtFixedRate(this::consumeTask, 1, 1, TimeUnit.SECONDS);
        }
        this.executor.scheduleAtFixedRate(this::submitTask, 1, 1, TimeUnit.SECONDS);
    }

    public void consumeTask() {
        while (true) {
            DispatchProcess dispatchProcess = TaskPoolUtil.offerDispatch();
            if (dispatchProcess != null) {
                this.dispatchTaskConsumer(dispatchProcess.getTask(), dispatchProcess.getDispatch());
            } else {
                break;
            }
        }
    }

    public void submitTask() {
        if (!this.sessionManager.isConnected()) {
            log.warn("当前与服务端断开连接,不执行上报任务.");
            return;
        }
        while (true) {
            SubmitTask submitTask = TaskPoolUtil.offerSubmit();
            if (submitTask != null) {
                if (!this.sessionManager.submitTask(submitTask)) {
                    TaskPoolUtil.pushSubmit(submitTask);
                    break;
                } else {
                    TaskPoolUtil.removeSubmit(submitTask.getTaskId());
                }
            } else {
                break;
            }
        }
    }
    @Override
    public void close() throws IOException {
        if (this.executor != null) {
            this.executor.shutdown();
        }
    }
}
