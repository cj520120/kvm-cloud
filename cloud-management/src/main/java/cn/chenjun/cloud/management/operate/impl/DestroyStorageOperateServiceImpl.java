package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostStorageOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyStorageOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 销毁存储池
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyStorageOperateServiceImpl extends AbstractOperateService<DestroyStorageOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyStorageOperate param) {
        StorageEntity storage = this.storageDao.findById(param.getStorageId());
        List<Integer> hostIds;
        if (Objects.equals(storage.getType(), cn.chenjun.cloud.common.util.Constant.StorageType.LOCAL)) {
            hostIds = Collections.singletonList(storage.getHostId());
        } else {
            List<HostEntity> hosts = hostDao.listByStatus(cn.chenjun.cloud.common.util.Constant.HostStatus.ONLINE);
            hostIds = hosts.stream().map(HostEntity::getHostId).collect(Collectors.toList());
        }
        DestroyHostStorageOperate operate = DestroyHostStorageOperate.builder().id(UUID.randomUUID().toString())
                .title(param.getTitle())
                .storageId(param.getStorageId())
                .nextHostIds(hostIds)
                .build();
        this.taskService.addTask(operate);
        this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyStorageOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            StorageEntity storage = storageDao.findById(param.getStorageId());
            if (storage != null && storage.getStatus() != cn.chenjun.cloud.common.util.Constant.StorageStatus.DESTROY) {
                storage.setStatus(Constant.StorageStatus.ERROR);
                storageDao.update(storage);
            }
        }

        this.notifyService.publish(NotifyData.<Void>builder().id(param.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.common.util.Constant.OperateType.DESTROY_STORAGE;
    }
}
