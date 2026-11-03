package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.data.entity.StorageEntity;
import cn.ruiyun.cloud.management.operate.bean.DestroyHostStorageOperate;
import cn.ruiyun.cloud.management.operate.bean.DestroyStorageOperate;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
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
        if (Objects.equals(storage.getType(), Constant.StorageType.LOCAL)) {
            hostIds = Collections.singletonList(storage.getHostId());
        } else {
            List<HostEntity> hosts = hostDao.listByStatus(Constant.HostStatus.ONLINE);
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
            if (storage != null && storage.getStatus() != Constant.StorageStatus.DESTROY) {
                storage.setStatus(Constant.StorageStatus.ERROR);
                storageDao.update(storage);
            }
        }

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_STORAGE;
    }
}
