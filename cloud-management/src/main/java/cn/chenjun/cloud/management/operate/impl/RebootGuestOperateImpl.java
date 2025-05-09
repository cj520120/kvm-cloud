package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestRebootRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.RebootGuestOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 重启虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class RebootGuestOperateImpl extends AbstractOperate<RebootGuestOperate, ResultUtil<Void>> {


    @Override
    public void operate(RebootGuestOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        HostEntity host = hostMapper.selectById(guest.getLastHostId());
        if (guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.REBOOT) {
            GuestRebootRequest request = GuestRebootRequest.builder().name(guest.getName()).build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_REBOOT, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "客户机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(RebootGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest != null && guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.REBOOT) {
            if (guest.getHostId() > 0) {
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
            } else {
                guest.setLastHostId(0);
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP);
            }
            guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyData.<ResultUtil<GuestEntity>>builder().id(param.getGuestId()).type(Constant.NotifyType.GUEST_RESTART_CALLBACK_NOTIFY).data(ResultUtil.<GuestEntity>builder().code(resultUtil.getCode()).message(resultUtil.getMessage()).data(guest).build()).build());

    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.REBOOT_GUEST;
    }
}
