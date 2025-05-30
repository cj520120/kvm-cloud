package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeDestroyRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyTemplateVolumeOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 销毁磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyTemplateVolumeOperateImpl extends AbstractOperate<DestroyTemplateVolumeOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyTemplateVolumeOperate param) {
        TemplateVolumeEntity volume = this.templateVolumeMapper.selectById(param.getVolumeId());
        if (volume != null) {
            StorageEntity storage = storageMapper.selectById(volume.getStorageId());
            HostEntity host = this.allocateService.allocateHost(0, storage.getHostId(), 0, 0);
            VolumeDestroyRequest request = VolumeDestroyRequest.builder()
                    .volume(initVolume(storage, volume))
                    .build();
            this.asyncInvoker(host, param, Constant.Command.VOLUME_DESTROY, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "模版磁盘不存在，ID:" + param.getVolumeId());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyTemplateVolumeOperate param, ResultUtil<Void> resultUtil) {
        TemplateVolumeEntity volume = templateVolumeMapper.selectById(param.getVolumeId());
        if (volume != null) {
            templateVolumeMapper.deleteById(param.getVolumeId());
        }

    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_TEMPLATE_VOLUME;
    }
}
