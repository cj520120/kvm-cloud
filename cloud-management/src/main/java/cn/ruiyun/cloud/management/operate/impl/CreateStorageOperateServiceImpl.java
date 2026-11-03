package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.data.entity.StorageEntity;
import cn.ruiyun.cloud.management.operate.bean.CreateStorageOperate;
import cn.ruiyun.cloud.management.operate.bean.InitHostStorageOperate;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 创建存储池
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateStorageOperateServiceImpl extends AbstractOperateService<CreateStorageOperate, ResultUtil<Void>> {


    @Override
    public void operate(CreateStorageOperate param) {
        StorageEntity storage = this.storageDao.findById(param.getStorageId());
        List<InitHostStorageOperate.HostStorageBind> binds = new ArrayList<>();
        if (storage.getType().equals(Constant.StorageType.LOCAL) && storage.getHostId() == 0) {
            List<StorageEntity> childStorages = this.storageDao.listStorageByParentStorageId(param.getStorageId());
            for (StorageEntity childStorage : childStorages) {
                binds.add(InitHostStorageOperate.HostStorageBind.builder().storageId(childStorage.getStorageId()).hostId(childStorage.getHostId()).build());
            }

        } else {
            List<HostEntity> hosts = hostDao.listAll()
                    .stream().filter(t -> {
                        switch (t.getStatus()) {
                            case Constant.HostStatus.REGISTER:
                            case Constant.HostStatus.ONLINE:
                                break;
                            default:
                                return false;
                        }
                        return !storage.getType().equalsIgnoreCase(Constant.StorageType.LOCAL) || Objects.equals(storage.getHostId(), t.getHostId());
                    }).collect(Collectors.toList());
            for (HostEntity host : hosts) {
                binds.add(InitHostStorageOperate.HostStorageBind.builder().storageId(param.getStorageId()).hostId(host.getHostId()).build());
            }

        }
        InitHostStorageOperate operate = InitHostStorageOperate.builder().id(UUID.randomUUID().toString())
                .title(param.getTitle())
                .storageId(param.getStorageId())
                .hostStorageBinds(binds)
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
    public void onFinish(CreateStorageOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            StorageEntity storage = storageDao.findById(param.getStorageId());
            if (storage != null && storage.getStatus() == Constant.StorageStatus.INIT) {
                storage.setStatus(Constant.StorageStatus.ERROR);
                storageDao.update(storage);
            }
        }

        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());

    }


    @Override
    public int getType() {
        return Constant.OperateType.CREATE_STORAGE;
    }
}
