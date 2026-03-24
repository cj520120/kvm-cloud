package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NoneRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.SyncHostTaskIdOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class SyncHostTaskIdOperateServiceImpl extends AbstractOperateService<SyncHostTaskIdOperate, ResultUtil<List<String>>> {


    @Override
    public void operate(SyncHostTaskIdOperate param) {
        HostEntity host = this.hostDao.findById(param.getHostId());
        this.asyncInvoker(host, param, Constant.Command.CHECK_TASK, NoneRequest.builder());
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<String>>>() {
        }.getType();
    }

    @Override
    public void onFinish(SyncHostTaskIdOperate param, ResultUtil<List<String>> resultUtil) {
        List<String> taskIds = resultUtil.getData();
        if (taskIds != null) {
            for (String taskId : taskIds) {
                taskService.keepTask(taskId);
            }
        }

    }

    @Override
    public int getType() {
        return Constant.OperateType.SYNC_HOST_TASK_ID;
    }
}
