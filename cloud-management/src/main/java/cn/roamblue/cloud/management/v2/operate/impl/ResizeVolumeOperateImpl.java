package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.bean.VolumeResizeRequest;
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
import cn.roamblue.cloud.management.v2.operate.bean.ResizeVolumeOperate;
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
public class ResizeVolumeOperateImpl extends AbstractOperate<ResizeVolumeOperate, ResultUtil<VolumeInfo>> {

    public ResizeVolumeOperateImpl() {
        super(ResizeVolumeOperate.class);
    }

    @Override
    public void operate(ResizeVolumeOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.CLONE) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            VolumeResizeRequest request = VolumeResizeRequest.builder()
                    .sourceStorage(storage.getName())
                    .sourceVolume(volume.getTarget())
                    .size(param.getSize())
                    .build();
            this.asyncCall(host, param, Constant.Command.VOLUME_RESIZE, request);
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
    public void onCallback(String hostId, ResizeVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        if (volume.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.RESIZE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setAllocation(resultUtil.getData().getAllocation());
            }
            volume.setStatus(cn.roamblue.cloud.management.v2.util.Constant.VolumeStatus.READY);
            volumeMapper.updateById(volume);
        }
    }
}
