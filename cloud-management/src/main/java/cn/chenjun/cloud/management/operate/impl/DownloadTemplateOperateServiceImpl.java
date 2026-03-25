package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDownloadRequest;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DownloadTemplateOperate;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class DownloadTemplateOperateServiceImpl extends AbstractOperateService<DownloadTemplateOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(DownloadTemplateOperate param) {
        TemplateVolumeEntity templateVolume = templateVolumeDao.findById(param.getTemplateVolumeId());
        TemplateEntity template = templateDao.findById(templateVolume.getTemplateId());
        if (template.getStatus() != Constant.TemplateStatus.DOWNLOAD) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]状态不是下载状态:" + template.getStatus());
        }
        if (templateVolume.getStatus() != Constant.TemplateStatus.DOWNLOAD) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]磁盘文件不是下载状态:" + template.getStatus());
        }
        try {
            StorageEntity storage = storageDao.findById(templateVolume.getStorageId());
            if (storage.getStatus() != Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            HostEntity host = this.allocateService.allocateHost(HostRole.NONE, 0, null, storage.getHostId(), 0, 0);
            StorageEntity targetStorage = storageDao.findById(templateVolume.getStorageId());

            VolumeDownloadRequest request = VolumeDownloadRequest.builder()
                    .sourceUri(template.getUri())
                    .md5(template.getMd5())
                    .volume(initVolume(targetStorage, templateVolume))
                    .build();
            this.asyncInvoker(host, param, Constant.Command.VOLUME_DOWNLOAD, request);
        } catch (CodeException e) {
            int code = e.getCode();
            switch (code) {
                case ErrorCode.STORAGE_NOT_READY:
                case ErrorCode.HOST_NOT_READY:
                case ErrorCode.HOST_NOT_RESOURCE:
                    throw new CodeException(ErrorCode.WAITING, e);
                default:
                    throw e;
            }
        }


    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<VolumeInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(DownloadTemplateOperate param, ResultUtil<VolumeInfo> resultUtil) {
        TemplateVolumeEntity templateVolume = templateVolumeDao.findById(param.getTemplateVolumeId());
        if (templateVolume != null) {
            TemplateEntity template = templateDao.findById(templateVolume.getTemplateId());
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                if (templateVolume.getStatus() == Constant.TemplateStatus.DOWNLOAD) {
                    templateVolume.setStatus(Constant.TemplateStatus.READY);
                    templateVolume.setCapacity(resultUtil.getData().getCapacity());
                    templateVolume.setAllocation(resultUtil.getData().getAllocation());
                    templateVolumeDao.update(templateVolume);
                }
                if (template != null && template.getStatus() == Constant.TemplateStatus.DOWNLOAD) {
                    template.setStatus(Constant.TemplateStatus.READY);
                    templateDao.update(template);
                }
            } else {
                if (templateVolume.getStatus() == Constant.TemplateStatus.DOWNLOAD) {
                    templateVolume.setStatus(Constant.TemplateStatus.ERROR);
                    templateVolumeDao.update(templateVolume);
                }
                if (template != null && template.getStatus() == Constant.TemplateStatus.DOWNLOAD) {
                    template.setStatus(Constant.TemplateStatus.ERROR);
                    templateDao.update(template);
                }
            }
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(templateVolume.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());

        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.DOWNLOAD_TEMPLATE;
    }
}
