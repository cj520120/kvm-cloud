package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.GuestNetworkEntity;
import cn.ruiyun.cloud.management.data.entity.VolumeEntity;
import cn.ruiyun.cloud.management.operate.bean.DestroyGuestOperate;
import cn.ruiyun.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.ruiyun.cloud.management.servcie.ComponentService;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

/**
 * 销毁磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyGuestOperateServiceImpl extends AbstractOperateService<DestroyGuestOperate, ResultUtil<Void>> {

    @Autowired
    private ComponentService componentService;

    @Override
    public void operate(DestroyGuestOperate param) {
        GuestEntity guest = this.guestDao.findById(param.getGuestId());
        if (guest == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机[" + param.getGuestId() + "]已经删除");
        }
        if (!guest.getStatus().equals(Constant.GuestStatus.DESTROY)) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "销毁虚拟机失败:[" + guest.getName() + ",虚拟机状态不正常：" + guest.getStatus());
        }
        this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(DestroyGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = this.guestDao.findById(param.getGuestId());
        if (guest == null || !guest.getStatus().equals(Constant.GuestStatus.DESTROY)) {
            return;
        }
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<GuestNetworkEntity> guestNetworkList = this.guestNetworkDao.listByAllocate(Constant.NetworkAllocateType.GUEST, guest.getGuestId());
            for (GuestNetworkEntity guestNetwork : guestNetworkList) {
                this.allocateService.releaseNetwork(guestNetwork.getGuestNetworkId());
            }
            List<VolumeEntity> volumes = this.volumeDao.listByGuestId(guest.getGuestId());

            for (VolumeEntity volume : volumes) {
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeDao.update(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).build();
                taskService.addTask(operate);
                NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(volume.getVolumeId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
            }
            this.configService.deleteAllocateConfig(Constant.ConfigType.GUEST, param.getGuestId());
            this.guestDao.deleteById(guest.getGuestId());
            this.configService.deleteAllocateConfig(Constant.ConfigType.GUEST, guest.getGuestId());
            this.componentService.deleteComponentGuest(guest.getGuestId());
            this.hostPciDeviceDao.deleteByGuestId(guest.getGuestId());
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(guest.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(guest.getNetworkId()).type(Constant.NotifyType.UPDATE_DNS).build());

        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_GUEST;
    }
}
