package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.OperateDispatch;
import cn.chenjun.cloud.agent.operate.bean.Consumer;
import cn.chenjun.cloud.agent.operate.bean.Dispatch;
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
import org.springframework.beans.factory.config.BeanPostProcessor;
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
public class OperateDispatchImpl implements OperateDispatch, BeanPostProcessor {
    @Autowired
    private DispatchFactory dispatchFactory;
    @Autowired
    private ConnectPool connectPool;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private ClientService clientService;

    @Override
    public ResultUtil<Void> submitTask(String data) {
        TaskRequest task = GsonBuilderUtil.create().fromJson(data, TaskRequest.class);
        log.info("提交异步任务:taskId={},command={},data={}", task.getTaskId(), task.getCommand(), task.getData());
        this.executor.submit(() -> {
            ResultUtil<?> result = null;
            try {
                result = dispatch(task.getTaskId(), task.getCommand(), task.getData());
            } catch (CodeException err) {
                result = ResultUtil.error(err.getCode(), err.getMessage());
            } catch (Exception err) {
                result = ResultUtil.error(ErrorCode.SERVER_ERROR, err.getMessage());
                log.error("执行任务出错.", err);
            } finally {
                try {
                    String nonce = String.valueOf(System.nanoTime());
                    Map<String, Object> map = new HashMap<>(5);
                    map.put("taskId", task.getTaskId());
                    map.put("data", GsonBuilderUtil.create().toJson(result));
                    map.put("timestamp", System.currentTimeMillis());
                    String sign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
                    map.put("sign", sign);
                    HttpUtil.post(clientService.getManagerUri() + "api/agent/task/report", map);
                } catch (Exception err) {
                    log.error("上报任务出现异常。command={} param={} result={}", task.getCommand(), task.getData(), result, err);
                } finally {
                    log.info("移除异步任务:{}", task.getTaskId());
                }

            }
        });
        return ResultUtil.success();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResultUtil<?> dispatch(String taskId, String command, String data) {

        TaskIdUtil.push(taskId);
        Connect connect = null;
        try {
            connect = connectPool.borrowObject();
            Dispatch<?, ?> dispatch = this.dispatchFactory.getDispatch(command);
            if (dispatch == null) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的操作:" + command);
            }
            Consumer consumer = dispatch.getConsumer();
            Object param = StringUtils.isEmpty(data) ? null : GsonBuilderUtil.create().fromJson(data, dispatch.getParamType());
            Object result = consumer.dispatch(connect, param);
            log.info("dispatch command={} param={} result={}", command, data, result);
            return ResultUtil.builder().data(result).build();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("dispatch fail. taskId={} command={} data={}", taskId, command, data, err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            TaskIdUtil.remove(taskId);
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }
}
