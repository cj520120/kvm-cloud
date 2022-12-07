package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeCreateRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.data.entity.VolumeEntity;
import cn.roamblue.cloud.management.data.mapper.TemplateVolumeMapper;
import cn.roamblue.cloud.management.operate.bean.CreateVolumeOperate;
import cn.roamblue.cloud.management.util.SpringContextUtils;
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

    public CreateVolumeOperateImpl() {
        super((Class<T>) CreateVolumeOperate.class);
    }

    public CreateVolumeOperateImpl(Class<T> tClass) {
        super(tClass);
    }

    @Override
    public void operate(T param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            if (storage.getStatus() != cn.roamblue.cloud.management.util.Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
            Collections.shuffle(hosts);
            HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
            VolumeCreateRequest request = VolumeCreateRequest.builder()
                    .targetStorage(storage.getName())
                    .targetVolume(volume.getPath())
                    .targetName(volume.getName())
                    .targetType(volume.getType())
                    .targetSize(volume.getCapacity())
                    .build();
            if (volume.getTemplateId() > 0) {
                TemplateVolumeMapper templateVolumeMapper = SpringContextUtils.getBean(TemplateVolumeMapper.class);
                List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", volume.getTemplateId()));
                Collections.shuffle(templateVolumeList);
                if (templateVolumeList.size() > 0) {
                    TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
                    StorageEntity parentStorage = storageMapper.selectById(templateVolume.getStorageId());
                    request.setParentStorage(parentStorage.getName());
                    request.setParentType(templateVolume.getType());
                    request.setParentVolume(templateVolume.getPath());
                }
            }
            this.asyncInvoker(host, param, Constant.Command.VOLUME_CREATE, request);
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
        if (volume.getStatus() == cn.roamblue.cloud.management.util.Constant.VolumeStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.READY);
            } else {
                volume.setStatus(cn.roamblue.cloud.management.util.Constant.VolumeStatus.ERROR);
            }
            volumeMapper.updateById(volume);
        }
    }
}
