package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.operate.process.DispatchFactory;
import cn.chenjun.cloud.agent.service.ConnectPool;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.agent.util.TaskIdUtil;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.TaskRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatch {
    @Autowired
    private DispatchFactory dispatchFactory;
    @Autowired
    private ConnectPool connectPool;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private ClientService clientService;


    public ResultUtil<Object> dispatch(String data) {
        TaskRequest task = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
        Dispatch<?, ?> dispatch = this.dispatchFactory.getDispatch(task.getCommand());
        if (dispatch == null) {
            return ResultUtil.error(ErrorCode.SERVER_ERROR, "不支持的操作:" + task.getCommand());
        }
        TaskIdUtil.push(task.getTaskId());
        if (dispatch.isAsync()) {
            this.executor.submit(() -> {
                dispatchTaskConsumer(task, dispatch);
            });
            return ResultUtil.builder().code(ErrorCode.AGENT_TASK_ASYNC_WAIT).build();
        } else {
            return dispatchTaskConsumer(task, dispatch);
        }
    }

    private ResultUtil<Object> dispatchTaskConsumer(TaskRequest task, Dispatch dispatch) {
        ResultUtil<Object> executeResult = null;
        Connect connect = null;
        try {
            long startTime = System.currentTimeMillis();
            connect = connectPool.borrowObject();
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
            TaskIdUtil.remove(task.getTaskId());
            if (dispatch.isAsync()) {
                submitTaskCallback(task, executeResult);
            }
            if (connect != null) {
                connectPool.returnObject(connect);
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
            map.put("timestamp", System.currentTimeMillis());
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

}
