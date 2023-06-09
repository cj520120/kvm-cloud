package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
public class DestroyTemplateOperateImpl extends AbstractOperate<DestroyTemplateOperate, ResultUtil<Void>> {

    public DestroyTemplateOperateImpl() {
        super(DestroyTemplateOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyTemplateOperate param) {
        TemplateEntity template = this.templateMapper.selectById(param.getTemplateId());
        if (template.getStatus() != cn.chenjun.cloud.management.util.Constant.TemplateStatus.DESTROY) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版[" + template.getName() + "]状态不正确:" + template.getStatus());
        }
        List<TemplateVolumeEntity> volumes = this.templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", param.getTemplateId()));
        if (!volumes.isEmpty()) {
            this.templateVolumeMapper.deleteBatchIds(volumes.stream().map(TemplateVolumeEntity::getTemplateVolumeId).collect(Collectors.toSet()));
        }
        this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        if (this.applicationConfig.isClearTemplateVolume() && !this.applicationConfig.isEnableVolumeBack()) {
            for (TemplateVolumeEntity volume : volumes) {
                StorageEntity storage = storageMapper.selectById(volume.getStorageId());
                if (storage != null) {
                    HostEntity host = this.allocateService.allocateHost(0, 0, 0, 0);
                    VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                            .sourceStorage(storage.getName())
                            .sourceVolume(volume.getPath())
                            .sourceType(volume.getType())
                            .build();
                    this.asyncInvoker(host, param, Constant.Command.VOLUME_DESTROY, request);
                }
                templateVolumeMapper.deleteById(volume);
            }
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyTemplateOperate param, ResultUtil<Void> resultUtil) {
        TemplateEntity template = this.templateMapper.selectById(param.getTemplateId());
        if (template.getStatus() == cn.chenjun.cloud.management.util.Constant.TemplateStatus.DESTROY) {
            this.templateMapper.deleteById(template);
            this.guestMapper.detachCdByTemplateId(template.getTemplateId());
            this.notifyService.publish(NotifyInfo.builder().id(param.getTemplateId()).type(Constant.NotifyType.UPDATE_TEMPLATE).build());
        }

    }
}
