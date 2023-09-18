package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.SocketMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostStorageOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyStorageOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
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
public class DestroyStorageOperateImpl extends AbstractOperate<DestroyStorageOperate, ResultUtil<Void>> {

    public DestroyStorageOperateImpl() {
        super(DestroyStorageOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyStorageOperate param) {
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>())
                .stream().filter(t -> Objects.equals(t.getStatus(), Constant.HostStatus.ONLINE)).collect(Collectors.toList());
        List<Integer> hostIds = hosts.stream().map(HostEntity::getHostId).collect(Collectors.toList());
        DestroyHostStorageOperate operate = DestroyHostStorageOperate.builder().taskId(UUID.randomUUID().toString())
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(DestroyStorageOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (storage.getStatus() != Constant.StorageStatus.DESTROY) {
                storage.setStatus(Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }

        this.notifyService.publish(SocketMessage.builder().id(param.getStorageId()).type(cn.chenjun.cloud.common.util.Constant.SocketCommand.UPDATE_STORAGE).build());
    }
}
