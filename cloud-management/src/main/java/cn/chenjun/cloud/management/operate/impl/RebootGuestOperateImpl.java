package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestRebootRequest;
import cn.chenjun.cloud.common.bean.SocketMessage;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.RebootGuestOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * 重启虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class RebootGuestOperateImpl extends AbstractOperate<RebootGuestOperate, ResultUtil<Void>> {

    public RebootGuestOperateImpl() {
        super(RebootGuestOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(RebootGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.REBOOT) {
            if (guest.getHostId() > 0) {
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
            } else {
                guest.setLastHostId(0);
                guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP);
            }
            guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(SocketMessage.builder().id(param.getGuestId()).type(Constant.SocketCommand.UPDATE_GUEST).build());
    }
}