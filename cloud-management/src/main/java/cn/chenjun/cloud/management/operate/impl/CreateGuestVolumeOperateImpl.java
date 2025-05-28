package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.VolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.chenjun.cloud.management.operate.bean.CreateGuestVolumeOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 创建磁盘
 *
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestVolumeOperateImpl  extends CreateVolumeOperateImpl<CreateGuestVolumeOperate> {

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CREATE_GUEST_VOLUME;
    }

    @Override
    public void onFinish(CreateGuestVolumeOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        if(resultUtil.getCode() == ErrorCode.SUCCESS){
            GuestEntity guest = this.guestMapper.selectById(param.getGuestId());
            VolumeEntity volume = this.volumeMapper.selectById(param.getVolumeId());
            BaseOperateParam operate = ChangeGuestDiskOperate.builder()
                    .deviceId(volume.getDeviceId()).deviceBus(volume.getDeviceDriver()).attach(true).volumeId(param.getVolumeId()).guestId(volume.getGuestId())
                    .id(UUID.randomUUID().toString())
                    .title("挂载磁盘[" + guest.getDescription() + "]").build();
            this.taskService.addTask(operate);
        }
    }
}
