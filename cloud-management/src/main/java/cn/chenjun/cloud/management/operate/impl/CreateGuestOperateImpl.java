package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartGuestOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestOperateImpl extends CreateVolumeOperateImpl<CreateGuestOperate> {


    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest != null) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                if (param.isStart()) {
                    if (Objects.equals(cn.chenjun.cloud.common.util.Constant.GuestType.USER, guest.getType())) {
                        guest.setStatus(cn.chenjun.cloud.common.util.Constant.GuestStatus.STARTING);
                        guestMapper.updateById(guest);
                        BaseOperateParam operateParam = StartGuestOperate.builder().id(UUID.randomUUID().toString()).title("启动系统主机[" + guest.getDescription() + "]").hostId(param.getHostId()).guestId(param.getGuestId()).build();
                        this.taskService.addTask(operateParam);
                    } else {
                        guest.setStatus(cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP);
                        guestMapper.updateById(guest);
                    }

                } else {
                    guest.setHostId(0);
                    guest.setLastHostId(0);
                    guest.setStatus(cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP);
                    guestMapper.updateById(guest);
                    this.allocateService.initHostAllocate();
                }
            } else {
                guest.setLastHostId(0);
                guest.setStatus(cn.chenjun.cloud.common.util.Constant.GuestStatus.ERROR);
                guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
            }
            this.notifyService.publish(NotifyData.<Void>builder().id(guest.getNetworkId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.COMPONENT_UPDATE_DNS).build());
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_GUEST;
    }
}
