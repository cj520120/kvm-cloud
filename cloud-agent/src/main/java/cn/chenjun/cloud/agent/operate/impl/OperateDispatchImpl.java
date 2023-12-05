package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.OperateDispatch;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
import cn.chenjun.cloud.agent.service.ConnectPool;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.TaskRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OperateDispatchImpl implements OperateDispatch, BeanPostProcessor {
    private final ConcurrentHashMap<String, Long> taskMap = new ConcurrentHashMap<>();
    @Autowired
    private DispatchFactory dispatchFactory;
    @Autowired
    private ConnectPool connectPool;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private ClientService clientService;

    private void submitTask(String taskId, String command, String data) {
        taskMap.put(taskId, System.currentTimeMillis());
        log.info("提交异步任务:taskId={},command={},data={}", taskId, command, data);
        this.executor.submit(() -> {
            ResultUtil<?> result = null;
            try {
                result = dispatch(taskId, command, data);
            } catch (CodeException err) {
                result = ResultUtil.error(err.getCode(), err.getMessage());
            } catch (Exception err) {
                result = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                log.error("执行任务出错.", err);
            } finally {
                try {
                    String nonce = String.valueOf(System.nanoTime());
                    Map<String, Object> map = new HashMap<>(5);
                    map.put("taskId", taskId);
                    map.put("data", GsonBuilderUtil.create().toJson(result));
                    map.put("timestamp", System.currentTimeMillis());
                    String sign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
                    map.put("sign", sign);
                    HttpUtil.post(clientService.getManagerUri() + "api/agent/task/report", map);
                } catch (Exception err) {
                    log.error("上报任务出现异常。command={} param={} result={}", command, data, result, err);
                } finally {
                    taskMap.remove(taskId);
                    log.info("移除异步任务:{}", taskId);
                }

            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResultUtil<?> dispatch(String taskId, String command, String data) {

        this.taskMap.put(taskId, System.currentTimeMillis());
        Connect connect = null;
        try {
            Object result = null;
            connect = connectPool.borrowObject();
            switch (command) {
                case Constant.Command.CHECK_TASK:
                    result = new ArrayList<>(taskMap.keySet());
                    break;
                case Constant.Command.SUBMIT_TASK:
                    TaskRequest taskRequest = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
                    this.submitTask(taskRequest.getTaskId(), taskRequest.getCommand(), taskRequest.getData());
                    break;
                default:
                    Dispatch<?, ?> dispatch = this.dispatchFactory.getDispatch(command);
                    if (dispatch == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + command);
                    }
                    Consumer consumer = dispatch.getConsumer();
                    Object param = StringUtils.isEmpty(data) ? null : GsonBuilderUtil.create().fromJson(data, dispatch.getParamType());
                    result = consumer.dispatch(connect, param);
                    log.info("dispatch command={} param={} result={}", command, data, result);
                    break;

            }
            return ResultUtil.builder().data(result).build();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("dispatch fail. taskId={} command={} data={}", taskId, command, data, err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            this.taskMap.remove(taskId);
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }
}
