package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.StorageInfo;
import cn.chenjun.cloud.common.bean.StorageInfoRequest;
import cn.chenjun.cloud.common.util.BootstrapType;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.StorageCheckOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StorageCheckOperateImpl extends AbstractOperate<StorageCheckOperate, ResultUtil<List<StorageInfo>>> {


    @Override
    public void operate(StorageCheckOperate param) {

        List<StorageEntity> storageList = this.storageMapper.selectList(new QueryWrapper<>()).stream().filter(t -> Objects.equals(t.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.READY)).collect(Collectors.toList());
        if (storageList.isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success(new ArrayList<>()));
        } else {
            List<StorageInfoRequest> requests = storageList.stream().map(t -> StorageInfoRequest.builder().name(t.getName()).build()).collect(Collectors.toList());
            HostEntity host = this.allocateService.allocateHost(0, BootstrapType.BIOS, 0, 0, 0);
            this.asyncInvoker(host, param, Constant.Command.BATCH_STORAGE_INFO, requests);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<StorageInfo>>>() {
        }.getType();
    }

    @Override
    public void onFinish(StorageCheckOperate param, ResultUtil<List<StorageInfo>> resultUtil) {

        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<StorageInfo> storageInfoList = resultUtil.getData();
            if (storageInfoList.isEmpty()) {
                return;
            }
            for (StorageInfo info : storageInfoList) {
                if (info == null) {
                    continue;
                }
                StorageEntity sourceEntity = this.storageMapper.selectOne(new QueryWrapper<StorageEntity>().eq(StorageEntity.STORAGE_NAME, info.getName()));
                if (sourceEntity != null) {
                    if (!Objects.equals(sourceEntity.getCapacity(), info.getCapacity()) ||
                            !Objects.equals(sourceEntity.getAvailable(), info.getAvailable()) ||
                            !Objects.equals(sourceEntity.getAllocation(), info.getAllocation())
                    ) {
                        StorageEntity updateStorage = StorageEntity.builder()
                                .storageId(sourceEntity.getStorageId())
                                .capacity(info.getCapacity())
                                .available(info.getAvailable())
                                .allocation(info.getAllocation())
                                .build();
                        this.storageMapper.updateById(updateStorage);
                        this.eventService.publish(NotifyData.<Void>builder().type(Constant.NotifyType.UPDATE_STORAGE).id(sourceEntity.getStorageId()).build());
                    }
                }
            }
        }
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.STORAGE_CHECK;
    }
}
