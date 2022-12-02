package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeDestroyRequest;
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
import cn.roamblue.cloud.management.v2.operate.bean.DestroyVolumeOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author chenjun
 */
public class DestroyVolumeOperateImpl extends AbstractOperate<DestroyVolumeOperate, ResultUtil<Void>> {

    protected DestroyVolumeOperateImpl() {
        super(DestroyVolumeOperate.class);
    }

    @Override
    public void operate(DestroyVolumeOperate param) {
        StorageMapper storageMapper = SpringContextUtils.getBean(StorageMapper.class);
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        VolumeEntity volume = volumeMapper.selectById(param.getId());
        StorageEntity storage = storageMapper.selectById(volume.getStorageId());
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<HostEntity>().eq("cluster_id", volume.getClusterId()));
        Collections.shuffle(hosts);
        HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.v2.util.Constant.HostStatus.READY, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
        VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                .sourceStorage(storage.getName())
                .sourceVolume(volume.getTarget())
                .sourceType(volume.getType())
                .build();
        this.asyncCall(host, param, Constant.Command.VOLUME_DESTROY, request);


    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, DestroyVolumeOperate param, ResultUtil<Void> resultUtil) {
        VolumeMapper volumeMapper = SpringContextUtils.getBean(VolumeMapper.class);
        volumeMapper.deleteById(param.getId());
    }
}
