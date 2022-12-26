package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeInfoRequest;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.operate.bean.VolumeCheckOperate;
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
import java.util.stream.Collectors;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class VolumeCheckOperateImpl extends AbstractOperate<VolumeCheckOperate, ResultUtil<List<VolumeInfo>>> {

    public VolumeCheckOperateImpl() {
        super(VolumeCheckOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(VolumeCheckOperate param) {
        StorageEntity storage = this.storageMapper.selectById(param.getStorageId());
        List<VolumeEntity> volumeList = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq("storage_id", param.getStorageId())).stream().filter(t -> Objects.equals(t.getStatus(), cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY)).collect(Collectors.toList());
        if (volumeList.isEmpty()) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success(new ArrayList<>()));
        } else {
            List<VolumeInfoRequest> requests = volumeList.stream().map(t -> VolumeInfoRequest.builder().sourceName(t.getName()).sourceVolume(t.getPath()).sourceStorage(storage.getName()).build()).collect(Collectors.toList());
            HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
            this.asyncInvoker(host, param, Constant.Command.BATCH_VOLUME_INFO, requests);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<VolumeInfo>>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(VolumeCheckOperate param, ResultUtil<List<VolumeInfo>> resultUtil) {

        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<VolumeInfo> volumeInfoList = resultUtil.getData();

            for (int i = 0; i < volumeInfoList.size(); i++) {
                VolumeInfo info = volumeInfoList.get(i);
                if (info == null) {
                    continue;
                }
                VolumeEntity sourceVolume = this.volumeMapper.selectOne(new QueryWrapper<VolumeEntity>().eq("volume_name", info.getName()));
                if (sourceVolume == null) {
                    continue;
                }

                if (!Objects.equals(sourceVolume.getCapacity(), info.getCapacity()) ||
                        !Objects.equals(sourceVolume.getAllocation(), info.getAllocation())
                ) {
                    VolumeEntity updateVolume = VolumeEntity.builder()
                            .volumeId(sourceVolume.getVolumeId())
                            .capacity(info.getCapacity())
                            .allocation(info.getAllocation())
                            .build();
                    this.volumeMapper.updateById(updateVolume);
                    this.notifyService.publish(NotifyInfo.builder().type(Constant.NotifyType.UPDATE_VOLUME).id(sourceVolume.getVolumeId()).build());
                }
            }
        }
    }
}
