package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.bean.VolumeDestroyRequest;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.data.entity.StorageEntity;
import cn.ruiyun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.ruiyun.cloud.management.operate.bean.DestroyTemplateVolumeOperate;
import cn.ruiyun.cloud.management.util.HostRole;
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
public class DestroyTemplateVolumeOperateServiceImpl extends AbstractOperateService<DestroyTemplateVolumeOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyTemplateVolumeOperate param) {
        TemplateVolumeEntity volume = this.templateVolumeDao.findById(param.getVolumeId());
        if (volume != null) {
            StorageEntity storage = storageDao.findById(volume.getStorageId());
            HostEntity host = this.allocateService.allocateHost(HostRole.NONE, 0, null, storage.getHostId(), 0, 0);
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
        TemplateVolumeEntity volume = templateVolumeDao.findById(param.getVolumeId());
        if (volume != null) {
            templateVolumeDao.deleteById(param.getVolumeId());
        }

    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_TEMPLATE_VOLUME;
    }
}
