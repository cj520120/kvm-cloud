package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCloneRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.CloneVolumeOperate;
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
public class CloneVolumeOperateImpl extends AbstractOperate<CloneVolumeOperate, ResultUtil<VolumeInfo>> {

    public CloneVolumeOperateImpl() {
        super(CloneVolumeOperate.class);
    }

    @Override
    public void operate(CloneVolumeOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CLONE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if(storage.getStatus()!= cn.roamblue.cloud.management.util.Constant.StorageStatus.READY){
                throw new CodeException(ErrorCode.STORAGE_NOT_READY,"存储池未就绪");
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            StorageEntity targetStorage = storageMapper.selectById(param.getTargetStorageId());
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getPath())
                    .targetStorage(targetStorage.getName())
                    .targetName(param.getTargetName())
                    .targetVolume(param.getTargetPath())
                    .targetType(param.getTargetType())

                    .build();

            this.asyncInvoker(host, param, Constant.Command.VOLUME_CLONE, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "原磁盘[" + volume.getName() + "]状态不正常:" + volume.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(CloneVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getSourceVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CLONE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                VolumeEntity targetVolume = VolumeEntity.builder()
                        .path(param.getTargetPath())
                        .clusterId(volume.getClusterId())
                        .storageId(param.getTargetStorageId())
                        .type(param.getTargetType())
                        .path(param.getTargetPath())
                        .name(param.getTargetName())
                        .templateId(volume.getTemplateId())
                        .allocation(resultUtil.getData().getAllocation())
                        .capacity(resultUtil.getData().getCapacity())
                        .status(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY)
                        .build();
                volumeMapper.insert(targetVolume);
            }
            volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
    }
}
