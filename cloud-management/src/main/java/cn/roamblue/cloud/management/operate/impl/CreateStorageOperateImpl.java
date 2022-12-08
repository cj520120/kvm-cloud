package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageCreateRequest;
import cn.roamblue.cloud.common.bean.StorageInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.operate.bean.CreateStorageOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 创建存储池
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateStorageOperateImpl extends AbstractOperate<CreateStorageOperate, ResultUtil<StorageInfo>> {

    public CreateStorageOperateImpl() {
        super(CreateStorageOperate.class);
    }

    @Override
    public void operate(CreateStorageOperate param) {
        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.INIT) {
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            ResultUtil<StorageInfo> resultUtil = null;
            for (HostEntity host : hosts) {
                if (Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                    Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storage.getParam(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    StorageCreateRequest request = StorageCreateRequest.builder()
                            .name(storage.getName())
                            .type(storage.getType())
                            .param(storageParam)
                            .mountPath(storage.getMountPath())
                            .build();
                    resultUtil = this.syncInvoker(host, param, Constant.Command.STORAGE_CREATE, request);
                    if (resultUtil.getCode() != ErrorCode.SUCCESS) {
                        break;
                    }
                }
            }
            this.onSubmitFinishEvent(param.getTaskId(), resultUtil);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<StorageInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(CreateStorageOperate param, ResultUtil<StorageInfo> resultUtil) {
        StorageEntity storage = storageMapper.selectById(param.getStorageId());

        if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.INIT) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                storage.setAllocation(resultUtil.getData().getAllocation());
                storage.setCapacity(resultUtil.getData().getCapacity());
                storage.setAvailable(resultUtil.getData().getAvailable());

                storage.setStatus(cn.roamblue.cloud.management.util.Constant.StorageStatus.READY);
            } else {
                storage.setStatus(cn.roamblue.cloud.management.util.Constant.StorageStatus.ERROR);
            }
            storageMapper.updateById(storage);
        }
    }

}
