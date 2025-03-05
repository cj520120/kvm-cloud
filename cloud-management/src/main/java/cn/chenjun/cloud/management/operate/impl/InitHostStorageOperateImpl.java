package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.InitHostStorageOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class InitHostStorageOperateImpl extends AbstractOperate<InitHostStorageOperate, ResultUtil<StorageInfo>> {


    @Override
    public void operate(InitHostStorageOperate param) {
        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (!Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());

        }
        if (param.getNextHostIds() == null || param.getNextHostIds().isEmpty()) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]无法创建，没有可用的主机分配");
        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<StorageInfo>builder().build());
            return;
        }
        List<ConfigQuery> queryList = new ArrayList<>();
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigType.DEFAULT).id(0).build());
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigType.STORAGE).id(storage.getStorageId()).build());
        queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigType.HOST).id(host.getHostId()).build());
        Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
        StorageCreateRequest request = buildStorageCreateRequest(storage, sysconfig);
        this.asyncInvoker(host, param, Constant.Command.STORAGE_CREATE, request);

    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<StorageInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(InitHostStorageOperate param, ResultUtil<StorageInfo> resultUtil) {
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {

            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            StorageInfo info = resultUtil.getData();

            if (storage != null && Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
                if (info != null) {
                    storage.setAllocation(resultUtil.getData().getAllocation());
                    storage.setCapacity(resultUtil.getData().getCapacity());
                    storage.setAvailable(resultUtil.getData().getAvailable());
                }
                if (!hostIds.isEmpty()) {
                    InitHostStorageOperate operate = InitHostStorageOperate.builder().id(UUID.randomUUID().toString())
                            .storageId(param.getStorageId())
                            .nextHostIds(hostIds)
                            .build();
                    this.taskService.addTask(operate);
                } else {
                    storage.setStatus(cn.chenjun.cloud.management.util.Constant.StorageStatus.READY);
                }
                storageMapper.updateById(storage);
            }
        } else {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (storage != null && Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
                storage.setStatus(cn.chenjun.cloud.management.util.Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());

    }


    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.INIT_HOST_STORAGE;
    }
}
