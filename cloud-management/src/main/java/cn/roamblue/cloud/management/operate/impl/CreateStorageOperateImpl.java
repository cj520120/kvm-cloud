package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.operate.bean.CreateStorageOperate;
import cn.roamblue.cloud.management.operate.bean.InitHostStorageOperate;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CreateStorageOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.INIT) {
                storage.setStatus(cn.roamblue.cloud.management.util.Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }

        this.notifyService.publish(NotifyInfo.builder().id(param.getStorageId()).type(cn.roamblue.cloud.common.util.Constant.NotifyType.UPDATE_STORAGE).build());

    }

}
