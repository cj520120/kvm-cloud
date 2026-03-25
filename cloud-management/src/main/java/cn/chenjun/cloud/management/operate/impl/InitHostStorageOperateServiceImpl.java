package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.InitHostStorageOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class InitHostStorageOperateServiceImpl extends AbstractOperateService<InitHostStorageOperate, ResultUtil<StorageInfo>> {


    @Override
    public void operate(InitHostStorageOperate param) {
        if (ObjectUtils.isEmpty(param.getHostStorageBinds())) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<StorageInfo>builder().build());
            return;
        }
        InitHostStorageOperate.HostStorageBind bind = param.getHostStorageBinds().get(0);
        StorageEntity storage = storageDao.findById(bind.getStorageId());
        HostEntity host = hostDao.findById(bind.getHostId());
        if (host == null || !Objects.equals(Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<StorageInfo>builder().build());
            return;
        }
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.DEFAULT).id(0).build());
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.STORAGE).id(storage.getStorageId()).build());
        queryList.add(ConfigQuery.builder().type(Constant.ConfigType.HOST).id(host.getHostId()).build());
        Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
        StorageCreateRequest request = buildStorageCreateRequest(storage, sysconfig);
        this.asyncInvoker(host, param, Constant.Command.STORAGE_CREATE, request);

    }

    @Override
    public boolean requireLock() {
        return true;
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<StorageInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(InitHostStorageOperate param, ResultUtil<StorageInfo> resultUtil) {

        InitHostStorageOperate.HostStorageBind bind = null;
        List<InitHostStorageOperate.HostStorageBind> hostStorageBinds = new ArrayList<>();
        if (param.getHostStorageBinds() != null && !param.getHostStorageBinds().isEmpty()) {
            hostStorageBinds.addAll(param.getHostStorageBinds());
            bind = hostStorageBinds.remove(0);
        }
        StorageEntity storage = bind == null ? null : storageDao.findById(bind.getStorageId());


        if (storage != null) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                StorageInfo info = resultUtil.getData();
                if (info != null) {
                    storage.setAllocation(info.getAllocation());
                    storage.setCapacity(info.getCapacity());
                    storage.setAvailable(info.getAvailable());
                }
            }
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                if (Objects.equals(storage.getStatus(), Constant.StorageStatus.INIT)) {
                    storage.setStatus(Constant.StorageStatus.READY);
                }
            } else if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                storage.setStatus(Constant.StorageStatus.ERROR);
            }
            storageDao.update(storage);
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
        }
        if (!hostStorageBinds.isEmpty()) {
            InitHostStorageOperate operate = InitHostStorageOperate.builder().id(UUID.randomUUID().toString())
                    .storageId(param.getStorageId())
                    .hostStorageBinds(hostStorageBinds)
                    .build();
            this.taskService.addTask(operate);
        } else {
            storage = storageDao.findById(param.getStorageId());
            if (storage != null) {
                if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                    storage.setStatus(Constant.StorageStatus.READY);
                } else {
                    storage.setStatus(Constant.StorageStatus.ERROR);
                }
            }
            storageDao.update(storage);
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(storage.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
        }
    }


    @Override
    public int getType() {
        return Constant.OperateType.INIT_HOST_STORAGE;
    }
}
