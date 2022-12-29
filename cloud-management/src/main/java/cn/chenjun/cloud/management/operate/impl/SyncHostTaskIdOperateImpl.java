package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.CreateHostOperate;
import cn.chenjun.cloud.management.operate.bean.SyncHostTaskIdOperate;
import cn.chenjun.cloud.management.task.OperateTask;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author chenjun
 */
@Component
@Slf4j
public class SyncHostTaskIdOperateImpl extends AbstractOperate<SyncHostTaskIdOperate, ResultUtil<List<String>>> {

    public SyncHostTaskIdOperateImpl() {
        super(SyncHostTaskIdOperate.class);
    }

    @Autowired
    @Lazy
    private OperateTask operateTask;

    @Override
    public void operate(SyncHostTaskIdOperate param) {
        HostEntity host = this.hostMapper.selectById(param.getHostId());
        this.asyncInvoker(host, param, Constant.Command.CHECK_TASK, new HashMap<>(0));
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
                operateTask.keepTask(taskId);
            }
        }

    }
}
