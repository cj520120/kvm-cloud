package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyGuestOperate;
import cn.chenjun.cloud.management.operate.bean.DestroyVolumeOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
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
public class DestroyGuestOperateImpl extends AbstractOperate<DestroyGuestOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyGuestOperate param) {
        GuestEntity guest = this.guestMapper.selectById(param.getGuestId());
        if (guest == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机[" + param.getGuestId() + "]已经删除");
        }
        if (!guest.getStatus().equals(cn.chenjun.cloud.common.util.Constant.GuestStatus.DESTROY)) {
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
        GuestEntity guest = this.guestMapper.selectById(param.getGuestId());
        if (guest == null || !guest.getStatus().equals(cn.chenjun.cloud.common.util.Constant.GuestStatus.DESTROY)) {
            return;
        }
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            List<GuestNetworkEntity> guestNetworkList = this.guestNetworkMapper.selectList(new QueryWrapper<GuestNetworkEntity>().eq(GuestNetworkEntity.ALLOCATE_ID, guest.getGuestId()).eq(GuestNetworkEntity.ALLOCATE_TYPE, cn.chenjun.cloud.common.util.Constant.NetworkAllocateType.GUEST));
            for (GuestNetworkEntity guestNetwork : guestNetworkList) {
                guestNetwork.setAllocateId(0);
                guestNetwork.setDeviceId(0);
                guestNetwork.setDriveType("");
                this.guestNetworkMapper.updateById(guestNetwork);
            }
            List<VolumeEntity> volumes = this.volumeMapper.selectList(new QueryWrapper<VolumeEntity>().eq(VolumeEntity.GUEST_ID, guest.getGuestId()));

            for (VolumeEntity volume : volumes) {
                volume.setStatus(Constant.VolumeStatus.DESTROY);
                volumeMapper.updateById(volume);
                DestroyVolumeOperate operate = DestroyVolumeOperate.builder().id(UUID.randomUUID().toString()).title("销毁磁盘[" + volume.getName() + "]").volumeId(volume.getVolumeId()).build();
                taskService.addTask(operate);
                this.notifyService.publish(NotifyData.<Void>builder().id(volume.getVolumeId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_VOLUME).build());
            }
            this.configService.deleteAllocateConfig(cn.chenjun.cloud.common.util.Constant.ConfigType.GUEST, param.getGuestId());
            this.guestMapper.deleteById(guest.getGuestId());
            this.configService.deleteAllocateConfig(cn.chenjun.cloud.common.util.Constant.ConfigType.GUEST, guest.getGuestId());
            this.notifyService.publish(NotifyData.<Void>builder().id(guest.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

            this.notifyService.publish(NotifyData.<Void>builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());

        }
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.common.util.Constant.OperateType.DESTROY_GUEST;
    }
}
