package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeDownloadRequest;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.StorageEntity;
import cn.roamblue.cloud.management.data.entity.TemplateEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DownloadTemplateOperateImpl extends AbstractOperate<DownloadTemplateOperate, ResultUtil<VolumeInfo>> {

    public DownloadTemplateOperateImpl() {
        super(DownloadTemplateOperate.class);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DownloadTemplateOperate param) {
        TemplateVolumeEntity templateVolume = templateVolumeMapper.selectById(param.getTemplateVolumeId());
        TemplateEntity template = templateMapper.selectById(templateVolume.getTemplateId());
        if (template.getStatus() != cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]状态不是下载状态:" + template.getStatus());
        }
        if (templateVolume.getStatus() != cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]磁盘文件不是下载状态:" + template.getStatus());
        }

        StorageEntity storage = storageMapper.selectById(templateVolume.getStorageId());
        if(storage.getStatus()!= cn.roamblue.cloud.management.util.Constant.StorageStatus.READY){
            throw new CodeException(ErrorCode.STORAGE_NOT_READY,"存储池未就绪");
        }
        List<HostEntity> hosts = hostMapper.selectList(new QueryWrapper<>());
        Collections.shuffle(hosts);
        HostEntity host = hosts.stream().filter(h -> Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, h.getStatus())).findFirst().orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "没有可用的主机信息"));
        StorageEntity targetStorage = storageMapper.selectById(templateVolume.getStorageId());

        VolumeDownloadRequest request = VolumeDownloadRequest.builder()
                .sourceUri(template.getUri())
                .targetStorage(targetStorage.getName())
                .targetName(templateVolume.getName())
                .targetVolume(templateVolume.getPath())
                .targetType(templateVolume.getType())
                .build();

        this.asyncInvoker(host, param, Constant.Command.VOLUME_DOWNLOAD, request);


    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(DownloadTemplateOperate param, ResultUtil<VolumeInfo> resultUtil) {
        TemplateVolumeEntity templateVolume = templateVolumeMapper.selectById(param.getTemplateVolumeId());
        TemplateEntity template = templateMapper.selectById(templateVolume.getTemplateId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            if (templateVolume.getStatus() == cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
                templateVolume.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.READY);
                templateVolume.setCapacity(resultUtil.getData().getCapacity());
                templateVolumeMapper.updateById(templateVolume);
            }
            if (template.getStatus() == cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
                template.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.READY);
                templateMapper.updateById(template);
            }
        } else {
            if (templateVolume.getStatus() == cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
                templateVolume.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.ERROR);
                templateVolumeMapper.updateById(templateVolume);
            }
            if (template.getStatus() == cn.roamblue.cloud.management.util.Constant.TemplateStatus.DOWNLOAD) {
                template.setStatus(cn.roamblue.cloud.management.util.Constant.TemplateStatus.ERROR);
                templateMapper.updateById(template);
            }
        }
    }
}