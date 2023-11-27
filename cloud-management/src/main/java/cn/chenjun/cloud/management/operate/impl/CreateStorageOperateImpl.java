package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.CreateStorageOperate;
import cn.chenjun.cloud.management.operate.bean.InitHostStorageOperate;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
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
public class CreateStorageOperateImpl extends AbstractOperate<CreateStorageOperate, ResultUtil<Void>> {

    public CreateStorageOperateImpl() {
        super(CreateStorageOperate.class);
    }


    @Override
    public void operate(CreateStorageOperate param) {
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>())
                .stream().filter(t -> Objects.equals(t.getStatus(), Constant.HostStatus.ONLINE)).collect(Collectors.toList());
        List<Integer> hostIds = hosts.stream().map(HostEntity::getHostId).collect(Collectors.toList());
        InitHostStorageOperate operate = InitHostStorageOperate.builder().taskId(UUID.randomUUID().toString())
                .title(param.getTitle())
                .storageId(param.getStorageId())
                .nextHostIds(hostIds)
                .build();
        this.operateTask.addTask(operate);
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
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (storage != null && storage.getStatus() == Constant.StorageStatus.INIT) {
                storage.setStatus(Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }

        this.clusterService.publish(NotifyData.builder().id(param.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());

    }

}
