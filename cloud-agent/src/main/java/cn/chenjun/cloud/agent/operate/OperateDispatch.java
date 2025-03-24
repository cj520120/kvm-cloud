package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.operate.bean.DispatchProcess;
import cn.chenjun.cloud.agent.operate.process.DispatchFactory;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.util.ConnectFactory;
import cn.chenjun.cloud.agent.util.TaskPoolUtil;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.TaskRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatch implements CommandLineRunner {
    @Autowired
    private DispatchFactory dispatchFactory;


    private ScheduledThreadPoolExecutor executor;
    @Autowired
    private ClientService clientService;

    @Autowired
    private ApplicationConfig applicationConfig;


    public ResultUtil<Object> dispatch(String data) {
        TaskRequest task = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
        Dispatch<?, ?> dispatch = this.dispatchFactory.getDispatch(task.getCommand());
        if (dispatch == null) {
            return ResultUtil.error(ErrorCode.SERVER_ERROR, "不支持的操作:" + task.getCommand());
        }
        if (dispatch.isAsync()) {

            DispatchProcess dispatchProcess = DispatchProcess.builder().dispatch(dispatch).task(task).build();
            TaskPoolUtil.push(dispatchProcess);
            log.info("提交异步任务:{}[{}]", task.getCommand(), task.getTaskId());
            return ResultUtil.builder().code(ErrorCode.AGENT_TASK_ASYNC_WAIT).build();
        } else {
            log.info("同步执行任务{}[{}]", task.getCommand(), task.getTaskId());
            return dispatchTaskConsumer(task, dispatch);
        }
    }

    private ResultUtil<Object> dispatchTaskConsumer(TaskRequest task, Dispatch dispatch) {
        ResultUtil<Object> executeResult = null;
        Connect connect = null;
        try {
            connect = ConnectFactory.create();
            log.info("开始执行任务:{}-{}",task.getCommand(),task.getTaskId());
            long startTime = System.currentTimeMillis();

            Object param = StringUtils.isEmpty(task.getData()) ? null : GsonBuilderUtil.create().fromJson(task.getData(), dispatch.getParamType());
            Consumer consumer = dispatch.getConsumer();
            Object result = consumer.dispatch(connect, param);
            log.info("dispatch async={} cost={}ms command={} param={} result={}", dispatch.isAsync(), System.currentTimeMillis() - startTime, task.getCommand(), task.getData(), result);
            executeResult = ResultUtil.success(result);
        } catch (CodeException err) {
            executeResult = ResultUtil.error(err.getCode(), err.getMessage());
        } catch (Exception err) {
            log.error("dispatch fail. taskId={} command={} data={}", task.getTaskId(), task.getCommand(), task.getData(), err);
            executeResult = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
        } finally {
            TaskPoolUtil.remove(task.getTaskId());
            if (dispatch.isAsync()) {
                submitTaskCallback(task, executeResult);
            }
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

    private void submitTaskCallback(TaskRequest task, ResultUtil<Object> resultUtil) {
        String result = GsonBuilderUtil.create().toJson(resultUtil);
        try {
            String nonce = String.valueOf(System.nanoTime());
            Map<String, Object> map = new HashMap<>(5);
            map.put("taskId", task.getTaskId());
            map.put("data", result);
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
            String sign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
            map.put("sign", sign);
            String url = clientService.getManagerUri();
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "api/agent/task/report";
            HttpUtil.post(url, map);
        } catch (Exception err) {
            log.error("上报任务出现异常.command={} param={} result={}", task.getCommand(), task.getData(), result, err);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        int taskSize = Math.max(this.applicationConfig.getTaskThreadSize(), 1);
        this.executor = new ScheduledThreadPoolExecutor(taskSize, new BasicThreadFactory.Builder().namingPattern("job-executor-pool-%d").daemon(true).build());
        for (int i = 0; i < taskSize; i++) {
            this.executor.scheduleAtFixedRate(this::consumeTask, 10, 1, TimeUnit.SECONDS);
        }
    }

    public void consumeTask() {
        DispatchProcess dispatchProcess = TaskPoolUtil.offer();
        if (dispatchProcess != null) {
            this.dispatchTaskConsumer(dispatchProcess.getTask(), dispatchProcess.getDispatch());
        }
    }
}
