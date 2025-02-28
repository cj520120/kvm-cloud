package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeCloneRequest;
import cn.chenjun.cloud.common.bean.VolumeCreateRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.CreateVolumeOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateVolumeOperateImpl<T extends CreateVolumeOperate> extends AbstractOperate<T, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(T param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            if (param.getTemplateId() > 0) {
                List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, param.getTemplateId()));
                Collections.shuffle(templateVolumeList);
                TemplateVolumeEntity templateVolume = templateVolumeList.stream().filter(t -> Objects.equals(t.getStatus(), cn.chenjun.cloud.management.util.Constant.TemplateStatus.READY)).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "当前模版未就绪"));
                StorageEntity parentStorage = storageMapper.selectById(templateVolume.getStorageId());

                VolumeCloneRequest request = VolumeCloneRequest.builder()
                        .sourceVolume(initVolume(parentStorage, templateVolume))
                        .targetVolume(initVolume(storage, volume))
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);

            } else if (param.getSnapshotVolumeId() > 0) {
                SnapshotVolumeEntity snapshotVolume = snapshotVolumeMapper.selectById(param.getSnapshotVolumeId());
                if (snapshotVolume.getStatus() != cn.chenjun.cloud.management.util.Constant.SnapshotStatus.READY) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "当前快照未就绪");
                }
                StorageEntity parentStorage = storageMapper.selectById(snapshotVolume.getStorageId());
                VolumeCloneRequest request = VolumeCloneRequest.builder()
                        .sourceVolume(initVolume(parentStorage, snapshotVolume))
                        .targetVolume(initVolume(storage, volume))
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);
            } else {
                VolumeCreateRequest request = VolumeCreateRequest.builder()
                        .volume(initVolume(storage, volume))
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_CREATE, request);
            }
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
        }

    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(T param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.chenjun.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setTemplateId(param.getTemplateId());
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setType(resultUtil.getData().getType());
                volume.setPath(resultUtil.getData().getPath());
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
            } else {
                volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(volume);
        }

        this.notifyService.publish(NotifyData.<Void>builder().id(param.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());

    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CREATE_VOLUME;
    }
}
