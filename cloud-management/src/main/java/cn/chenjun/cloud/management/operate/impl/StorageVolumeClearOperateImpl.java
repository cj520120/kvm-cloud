package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.DestroyUnLinkVolumeRequest;
import cn.chenjun.cloud.common.bean.ListStorageVolumeRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.StorageVolumeCleanOperate;
import cn.chenjun.cloud.management.util.HostRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StorageVolumeClearOperateImpl extends AbstractOperate<StorageVolumeCleanOperate, ResultUtil<List<String>>> {


    @Override
    public void operate(StorageVolumeCleanOperate param) {
        StorageEntity storage = this.storageMapper.selectById(param.getStorageId());
        if (storage == null) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.STORAGE_NOT_FOUND, "存储池不存在"));
        } else {
            ListStorageVolumeRequest request = ListStorageVolumeRequest.builder().name(storage.getName()).build();
            HostEntity host = this.allocateService.allocateHost(HostRole.NONE,0, storage.getHostId(), 0, 0);
            this.asyncInvoker(host, param, Constant.Command.LIST_STORAGE_VOLUME, request);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<String>>>() {
        }.getType();
    }

    @Override
    public void onFinish(StorageVolumeCleanOperate param, ResultUtil<List<String>> resultUtil) {

        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<String> volumeNames = resultUtil.getData();
            List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, param.getStorageId()));
            List<String> useVolumeNames = new ArrayList<>(volumeList.stream().map(VolumeEntity::getName).collect(Collectors.toList()));
            List<TemplateVolumeEntity> templateVolumeList = this.templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.STORAGE_ID, param.getStorageId()));
            useVolumeNames.addAll(templateVolumeList.stream().map(TemplateVolumeEntity::getName).collect(Collectors.toList()));
            List<String> destroyVolumeNames = new ArrayList<>();
            for (String volumeName : volumeNames) {
                if (useVolumeNames.contains(volumeName)) {
                    continue;
                }
                destroyVolumeNames.add(volumeName);
            }
            if (!destroyVolumeNames.isEmpty()) {
                log.info("开始清理未关联磁盘:{}", destroyVolumeNames);
                StorageEntity storage = storageMapper.selectById(param.getStorageId());
                if (storage != null) {
                    HostEntity host = this.allocateService.allocateHost(HostRole.NONE,0, storage.getHostId(), 0, 0);
                    DestroyUnLinkVolumeRequest request = DestroyUnLinkVolumeRequest.builder()
                            .storage(storage.getName())
                            .volumes(destroyVolumeNames)
                            .build();
                    this.asyncInvoker(host, StorageVolumeCleanOperate.builder().title("清理无效磁盘").storageId(param.getStorageId()).id(UUID.randomUUID().toString()).build(), Constant.Command.DESTROY_UNLINK_VOLUME, request);
                }
            } else {
                log.info("存储池[{}]所有磁盘均有有效引用，无需处理", param.getStorageId());
            }
        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.STORAGE_VOLUME_CLEAR;
    }
}
