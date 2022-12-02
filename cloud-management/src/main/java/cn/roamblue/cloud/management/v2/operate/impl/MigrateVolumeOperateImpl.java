package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCloneRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.*;
import cn.roamblue.cloud.management.v2.data.mapper.GuestDiskMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.v2.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.CloneVolumeOperate;
import cn.roamblue.cloud.management.v2.operate.bean.DestroyStorageOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
public class MigrateVolumeOperateImpl extends AbstractOperate<CloneVolumeOperate, ResultUtil<VolumeInfo>> {

    protected MigrateVolumeOperateImpl() {
        super(CloneVolumeOperate.class);
    }

    @Override
    public void operate(CloneVolumeOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.MIGRATE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            StorageEntity targetStorage=storageMapper.selectById(param.getTargetStorageId());
            VolumeCloneRequest request = VolumeCloneRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getTarget())
                    .targetStorage(targetStorage.getName())
                    .targetVolume(param.getTargetVolume())
                    .targetType(param.getTargetType())

                    .build();

            this.asyncCall(host, param, Constant.Command.VOLUME_CLONE, request);
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
    public void onCallback(String hostId, CloneVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.MIGRATE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                VolumeEntity targetVolume=VolumeEntity.builder()
                        .target(param.getTargetVolume())
                        .clusterId(volume.getClusterId())
                        .type(param.getTargetType())
                        .target(param.getTargetVolume())
                        .name(param.getTargetName())
                        .parentId(0)
                        .allocation(resultUtil.getData().getAllocation())
                        .capacity(resultUtil.getData().getCapacity())
                        .status(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY)
                        .build();
                volumeMapper.insert(targetVolume);
                GuestDiskMapper guestDiskMapper=SpringContextUtils.getBean(GuestDiskMapper.class);
                GuestDiskEntity guestDisk= guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id",volume.getId()));
                if(guestDisk!=null){
                    guestDisk.setVolumeId(targetVolume.getId());
                    guestDiskMapper.updateById(guestDisk);
                }
                //提交源磁盘的销毁任务
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyStorageOperate operate=DestroyStorageOperate.builder().taskId(UUID.randomUUID().toString()).id(volume.getId()).build();
                OperateFactory.submitTask(operate);

            }else {
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
                volumeMapper.updateById(volume);
            }
        }
    }
}
