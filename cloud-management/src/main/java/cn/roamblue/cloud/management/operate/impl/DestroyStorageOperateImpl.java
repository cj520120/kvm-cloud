package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.StorageDestroyRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyStorageOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyStorageOperate param) {
        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.DESTROY) {
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());

            for (HostEntity host : hosts) {
                if (Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
                    StorageDestroyRequest request = StorageDestroyRequest.builder()
                            .name(storage.getName())
                            .build();
                    this.syncInvoker(host, param, Constant.Command.STORAGE_DESTROY, request);
                }
            }
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<Void>builder().build());
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());
        }
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
        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.DESTROY) {
            storageMapper.deleteById(param.getStorageId());
            templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq("storage_id", param.getStorageId()));
        }
    }
}
