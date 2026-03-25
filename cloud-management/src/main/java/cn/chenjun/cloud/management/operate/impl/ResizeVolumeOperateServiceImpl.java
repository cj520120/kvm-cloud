package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.bean.VolumeResizeRequest;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ResizeVolumeOperate;
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
public class ResizeVolumeOperateServiceImpl extends AbstractOperateService<ResizeVolumeOperate, ResultUtil<VolumeInfo>> {


    @Override
    public void operate(ResizeVolumeOperate param) {
        VolumeEntity volume = volumeDao.findById(param.getVolumeId());
        if (volume.getStatus() == Constant.VolumeStatus.RESIZE) {
            StorageEntity storage = storageDao.findById(volume.getStorageId());
            if (storage.getStatus() != Constant.StorageStatus.READY) {
                throw new CodeException(ErrorCode.STORAGE_NOT_READY, "存储池未就绪");
            }
            GuestEntity guest = this.guestDao.findById(volume.getGuestId());
            int hostId = 0;
            String vm = "";
            HostEntity host = null;
            if (guest != null && guest.getHostId() > 0) {
                host = this.hostDao.findById(guest.getHostId());
                vm = guest.getName();
            } else {
                host = this.allocateService.allocateHost(HostRole.NONE, hostId, null, volume.getHostId(), 0, 0);
            }
            VolumeResizeRequest request = VolumeResizeRequest.builder()
                    .vm(vm)
                    .volume(initVolume(storage, volume))
                    .size(param.getSize())
                    .build();
            this.asyncInvoker(host, param, Constant.Command.VOLUME_RESIZE, request);
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
    public void onFinish(ResizeVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        VolumeEntity volume = volumeDao.findById(param.getVolumeId());
        if (volume != null && volume.getStatus() == Constant.VolumeStatus.RESIZE) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                volume.setCapacity(resultUtil.getData().getCapacity());
                volume.setAllocation(resultUtil.getData().getAllocation());
                volume.setType(resultUtil.getData().getType());
                volume.setPath(resultUtil.getData().getPath());
            }
            volume.setStatus(Constant.VolumeStatus.READY);
            volumeDao.update(volume);
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.RESIZE_VOLUME;
    }
}
