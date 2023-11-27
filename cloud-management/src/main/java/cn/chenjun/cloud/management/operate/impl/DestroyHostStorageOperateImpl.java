package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.StorageDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.SnapshotVolumeEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostStorageOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyHostStorageOperateImpl extends AbstractOperate<DestroyHostStorageOperate, ResultUtil<Void>> {

    public DestroyHostStorageOperateImpl() {
        super(DestroyHostStorageOperate.class);
    }


    @Override
    public void operate(DestroyHostStorageOperate param) {

        StorageEntity storage = storageMapper.selectById(param.getStorageId());
        if (!Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.DESTROY)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "存储池[" + storage.getName() + "]状态不正确:" + storage.getStatus());

        }
        HostEntity host = hostMapper.selectById(param.getNextHostIds().get(0));
        if (host == null || !Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
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
                if (storage != null && storage.getStatus() == cn.chenjun.cloud.management.util.Constant.StorageStatus.DESTROY) {
                    storageMapper.deleteById(param.getStorageId());
                    templateVolumeMapper.delete(new QueryWrapper<TemplateVolumeEntity>().eq("storage_id", param.getStorageId()));
                    snapshotVolumeMapper.delete(new QueryWrapper<SnapshotVolumeEntity>().eq("storage_id", param.getStorageId()));
                }
            }
        } else {
            StorageEntity storage = storageMapper.selectById(param.getStorageId());
            if (storage != null && Objects.equals(storage.getStatus(), cn.chenjun.cloud.management.util.Constant.StorageStatus.DESTROY)) {
                storage.setStatus(cn.chenjun.cloud.management.util.Constant.StorageStatus.ERROR);
                storageMapper.updateById(storage);
            }
        }

        this.clusterService.publish(NotifyData.builder().id(param.getStorageId()).type(Constant.NotifyType.UPDATE_STORAGE).build());
    }
}
