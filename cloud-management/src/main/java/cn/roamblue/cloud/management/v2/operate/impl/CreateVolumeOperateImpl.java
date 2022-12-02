package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCreateRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.StorageEntity;
import cn.roamblue.cloud.management.v2.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.StorageMapper;
import cn.roamblue.cloud.management.v2.data.mapper.VolumeMapper;
import cn.roamblue.cloud.management.v2.operate.bean.CreateVolumeOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
public class CreateVolumeOperateImpl extends AbstractOperate<CreateVolumeOperate, ResultUtil<VolumeInfo>> {

    protected CreateVolumeOperateImpl() {
        super(CreateVolumeOperate.class);
    }

    @Override
    public void operate(CreateVolumeOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.CREATING) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));

            VolumeCreateRequest request = VolumeCreateRequest.builder()
                    .targetStorage(storage.getName())
                    .targetVolume(volume.getTarget())
                    .targetName(volume.getName())
                    .targetType(volume.getType())
                    .targetSize(volume.getAllocation())
                    .build();
            if (volume.getParentId() > 0) {
                VolumeEntity parentVolume = volumeMapper.selectById(volume.getParentId());
                StorageEntity parentStorage = storageMapper.selectById(volume.getStorageId());
                request.setParentVolume(parentStorage.getName());
                request.setParentType(parentVolume.getType());
                request.setParentVolume(parentVolume.getTarget());
            }
            this.asyncCall(host, param, Constant.Command.VOLUME_CREATE, request);
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
    public void onCallback(String hostId, CreateVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
            } else {
                volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(volume);
        }
    }
}
