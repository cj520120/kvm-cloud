package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.operate.bean.DispatchProcess;
import cn.chenjun.cloud.agent.operate.bean.SubmitTask;
import cn.chenjun.cloud.agent.operate.process.DispatchFactory;
import cn.chenjun.cloud.agent.util.ClientService;
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
    private ClientService clientService;

    @Autowired
    private ApplicationConfig applicationConfig;


    public <T, V> ResultUtil<T> dispatch(String data) {
        TaskRequest task = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
        Dispatch<T, V> dispatch = this.dispatchFactory.getDispatch(task.getCommand());
        if (dispatch == null) {
            return ResultUtil.error(ErrorCode.SERVER_ERROR, "不支持的操作:" + task.getCommand());
        }
        if (dispatch.isAsync()) {
            DispatchProcess dispatchProcess = DispatchProcess.builder().dispatch(dispatch).task(task).build();
            TaskPoolUtil.pushDispatch(dispatchProcess);
            log.info("提交异步任务:{}[{}]", task.getCommand(), task.getTaskId());
            return ResultUtil.<T>builder().code(ErrorCode.AGENT_TASK_ASYNC_WAIT).build();
        } else {
            log.info("同步执行任务{}[{}]", task.getCommand(), task.getTaskId());
            return dispatchTaskConsumer(task, dispatch);
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
            log.info("dispatch   cost={}ms command={} param={} result={}", System.currentTimeMillis() - startTime, task.getCommand(), task.getData(), result);
            executeResult = ResultUtil.success(result);
        } catch (CodeException err) {
            executeResult = ResultUtil.error(err.getCode(), err.getMessage());
        } catch (Exception err) {
            log.error("dispatch fail. taskId={} command={} data={}", task.getTaskId(), task.getCommand(), task.getData(), err);
            executeResult = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        } finally {
//            this.submitTaskCallback(task, executeResult);
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

//    private <T> void submitTaskCallback(TaskRequest task, ResultUtil<T> resultUtil) {
//        String result = GsonBuilderUtil.create().toJson(resultUtil);
//        try {
//            String nonce = String.valueOf(System.nanoTime());
//            Map<String, Object> map = new HashMap<>(5);
//            map.put("taskId", task.getTaskId());
//            map.put("data", result);
//            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
//            String sign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
//            map.put("sign", sign);
//            String url = clientService.getManagerUri();
//            if (!url.endsWith("/")) {
//                url += "/";
//            }
//            url += "api/agent/task/report";
//            HttpUtil.post(url, map);
//        } catch (Exception err) {
//            log.error("上报任务出现异常.command={} param={} result={}", task.getCommand(), task.getData(), result, err);
//        }
//    }

    @Override
    public void run(String... args) throws Exception {
        int taskSize = Math.max(this.applicationConfig.getTaskThreadSize(), 1);
        this.executor = new ScheduledThreadPoolExecutor(taskSize, new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
        for (int i = 0; i < taskSize; i++) {
            this.executor.scheduleAtFixedRate(this::consumeTask, 10, 1, TimeUnit.SECONDS);
        }
        this.executor.scheduleAtFixedRate(this::submitTask, 10, 1, TimeUnit.SECONDS);
    }

    public void consumeTask() {
        DispatchProcess dispatchProcess = TaskPoolUtil.offerDispatch();
        if (dispatchProcess != null) {
            this.dispatchTaskConsumer(dispatchProcess.getTask(), dispatchProcess.getDispatch());
        }
    }

    public void submitTask() {
        if (!this.sessionManager.isConnected()) {
            log.warn("当前与服务端断开连接,不执行上报任务.");
            return;
        }
        SubmitTask submitTask = TaskPoolUtil.offerSubmit();
        if (submitTask != null) {
            if (!this.sessionManager.submitTask(submitTask)) {
                TaskPoolUtil.pushSubmit(submitTask);
            } else {
                TaskPoolUtil.removeSubmit(submitTask.getTaskId());
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
