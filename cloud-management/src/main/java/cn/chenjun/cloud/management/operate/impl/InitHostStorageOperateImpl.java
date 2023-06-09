package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.StorageCreateRequest;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.InitHostStorageOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class InitHostStorageOperateImpl extends AbstractOperate<InitHostStorageOperate, ResultUtil<StorageInfo>> {

    public InitHostStorageOperateImpl() {
        super(InitHostStorageOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(InitHostStorageOperate param) {
        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (!Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());

        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<StorageInfo>builder().build());
            return;
        }
        Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
        }.getType());
        StorageCreateRequest request = StorageCreateRequest.builder()
                .name(storage.getName())
                .type(storage.getType())
                .param(storageParam)
                .mountPath(storage.getMountPath())
                .build();
        this.asyncInvoker(host, param, Constant.Command.STORAGE_CREATE, request);

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<StorageInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(InitHostStorageOperate param, ResultUtil<StorageInfo> resultUtil) {
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {

            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            StorageInfo info = resultUtil.getData();

            if (Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
                if (info != null) {
                    storage.setAllocation(resultUtil.getData().getAllocation());
                    storage.setCapacity(resultUtil.getData().getCapacity());
                    storage.setAvailable(resultUtil.getData().getAvailable());
                }
                if (!hostIds.isEmpty()) {
                    InitHostStorageOperate operate = InitHostStorageOperate.builder().taskId(UUID.randomUUID().toString())
                            .storageId(param.getStorageId())
                            .nextHostIds(hostIds)
                            .build();
                    this.operateTask.addTask(operate);
                } else {
                    storage.setStatus(cn.chenjun.cloud.management.util.Constant.StorageStatus.READY);
                }
                storageMapper.updateById(storage);
            }
        } else {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.INIT)) {
                storage.setStatus(cn.chenjun.cloud.management.util.Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());

    }

}
