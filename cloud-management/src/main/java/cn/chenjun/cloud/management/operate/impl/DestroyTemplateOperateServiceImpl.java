package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateOperate;
import cn.chenjun.cloud.management.util.HostRole;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销毁磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyTemplateOperateServiceImpl extends AbstractOperateService<DestroyTemplateOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyTemplateOperate param) {
        TemplateEntity template = this.templateDao.findById(param.getTemplateId());
        if (template.getStatus() != Constant.TemplateStatus.DESTROY) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]状态不正确:" + template.getStatus());
        }
        List<TemplateVolumeEntity> volumes = this.templateVolumeDao.listByTemplateId(param.getTemplateId());
        if (!volumes.isEmpty()) {
            this.templateVolumeDao.deleteBatchIds(volumes.stream().map(TemplateVolumeEntity::getTemplateVolumeId).collect(Collectors.toSet()));
        }
        this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        for (TemplateVolumeEntity volume : volumes) {
            StorageEntity storage = storageDao.findById(volume.getStorageId());
            if (storage != null) {
                HostEntity host = this.allocateService.allocateHost(HostRole.NONE, 0, null, storage.getHostId(), 0, 0);
                VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                        .volume(initVolume(storage, volume))
                        .build();
                this.asyncInvoker(host, param, Constant.Command.VOLUME_DESTROY, request);
            }
            templateVolumeDao.deleteById(volume.getTemplateVolumeId());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyTemplateOperate param, ResultUtil<Void> resultUtil) {
        TemplateEntity template = this.templateDao.findById(param.getTemplateId());
        if (template != null && template.getStatus() == Constant.TemplateStatus.DESTROY) {
            this.templateDao.deleteById(template.getTemplateId());
            this.guestDao.detachCdByTemplateId(template.getTemplateId());
            this.notifyService.publish(NotifyData.<Void>builder().id(param.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        }

    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_TEMPLATE;
    }
}
