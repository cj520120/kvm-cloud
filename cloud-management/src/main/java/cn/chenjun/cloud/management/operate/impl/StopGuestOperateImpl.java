package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.StopGuestOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 停止虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StopGuestOperateImpl extends AbstractOperate<StopGuestOperate, ResultUtil<Void>> {

    public StopGuestOperateImpl() {
        super(StopGuestOperate.class);
    }



    @Override
    public void operate(StopGuestOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() != cn.chenjun.cloud.management.util.Constant.GuestStatus.STOPPING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "客户机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
        HostEntity host = hostMapper.selectById(guest.getHostId());
        if (host == null) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        } else {
            if (!param.isForce()) {
                GuestShutdownRequest request = GuestShutdownRequest.builder().name(guest.getName()).build();
                this.asyncInvoker(host, param, Constant.Command.GUEST_SHUTDOWN, request);
            } else {
                GuestDestroyRequest request = GuestDestroyRequest.builder().name(guest.getName()).build();
                this.asyncInvoker(host, param, Constant.Command.GUEST_DESTROY, request);
            }
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }


    @Override
    public void onFinish(StopGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest != null && guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.STOPPING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setLastHostId(guest.getHostId());
                guest.setHostId(0);
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP);
            } else {
                guest.setLastHostId(guest.getHostId());
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
            }
            guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();

        }
        this.clusterService.publish(NotifyData.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }
}