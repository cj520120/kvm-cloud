package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeInfoRequest;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.VolumeCheckOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StorageVolumeCheckOperateImpl extends AbstractOperate<VolumeCheckOperate, ResultUtil<List<VolumeInfo>>> {


    @Override
    public void operate(VolumeCheckOperate param) {
        StorageEntity storage = this.storageMapper.selectById(param.getStorageId());
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.STORAGE_ID, param.getStorageId())).stream().filter(t -> Objects.equals(t.getStatus(), Constant.VolumeStatus.READY)).collect(Collectors.toList());
        if (volumeList.isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success(new ArrayList<>()));
        } else {
            List<VolumeInfoRequest> requests = volumeList.stream().map(t -> VolumeInfoRequest.builder().sourceName(t.getName()).sourceStorage(storage.getName()).build()).collect(Collectors.toList());
            HostEntity host = this.allocateService.allocateHost(0, storage.getHostId(), 0, 0);
            this.asyncInvoker(host, param, Constant.Command.BATCH_VOLUME_INFO, requests);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<VolumeInfo>>>() {
        }.getType();
    }

    @Override
    public void onFinish(VolumeCheckOperate param, ResultUtil<List<VolumeInfo>> resultUtil) {

        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<VolumeInfo> volumeInfoList = resultUtil.getData();

            for (VolumeInfo info : volumeInfoList) {
                if (info == null) {
                    continue;
                }
                VolumeEntity sourceVolume = this.volumeMapper.selectOne(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.VOLUME_NAME, info.getName()));
                if (sourceVolume == null) {
                    continue;
                }

                if (!Objects.equals(sourceVolume.getCapacity(), info.getCapacity()) ||
                        !Objects.equals(sourceVolume.getAllocation(), info.getAllocation()) ||
                        !Objects.equals(sourceVolume.getType(), info.getType()) ||
                        !Objects.equals(sourceVolume.getPath(), info.getPath())
                ) {
                    VolumeEntity updateVolume = VolumeEntity.builder()
                            .volumeId(sourceVolume.getVolumeId())
                            .capacity(info.getCapacity())
                            .allocation(info.getAllocation())
                            .type(info.getType())
                            .path(info.getPath())
                            .build();
                    this.volumeMapper.updateById(updateVolume);
                    this.notifyService.publish(NotifyData.<Void>builder().type(Constant.NotifyType.UPDATE_VOLUME).id(sourceVolume.getVolumeId()).build());
                }
            }
        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.VOLUME_CHECK;
    }
}
