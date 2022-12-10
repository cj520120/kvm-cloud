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
import cn.roamblue.cloud.management.operate.bean.DestroyHostStorageOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 销毁存储池
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyHostStorageOperateImpl extends AbstractOperate<DestroyHostStorageOperate, ResultUtil<Void>> {

    public DestroyHostStorageOperateImpl() {
        super(DestroyHostStorageOperate.class);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyHostStorageOperate param) {

        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (!Objects.equals(storage.getStatus(), cn.roamblue.cloud.management.util.Constant.StorageStatus.INIT)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());

        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            //主机未就绪直接提交成功
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
            return;
        }
        StorageDestroyRequest request = StorageDestroyRequest.builder().name(storage.getName()).build();
        this.asyncInvoker(host, param, Constant.Command.STORAGE_DESTROY, request);
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(DestroyHostStorageOperate param, ResultUtil<Void> resultUtil) {
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<Integer> hostIds = new ArrayList<>(param.getNextHostIds());
            if (!hostIds.isEmpty()) {
                hostIds.remove(0);
            }

            if (!hostIds.isEmpty()) {
                DestroyHostStorageOperate operate = DestroyHostStorageOperate.builder().taskId(UUID.randomUUID().toString())
                        .storageId(param.getStorageId())
                        .nextHostIds(hostIds)
                        .build();
                this.operateTask.addTask(operate);
            } else {
                StorageEntity storage = storageMapper.selectById(param.getStorageId());
                if (storage.getStatus() == cn.roamblue.cloud.management.util.Constant.StorageStatus.DESTROY) {
                    storageMapper.deleteById(param.getStorageId());
                    templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq("storage_id", param.getStorageId()));
                }
            }
        } else {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (Objects.equals(storage.getStatus(), cn.roamblue.cloud.management.util.Constant.StorageStatus.DESTROY)) {
                storage.setStatus(cn.roamblue.cloud.management.util.Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }
    }
}
