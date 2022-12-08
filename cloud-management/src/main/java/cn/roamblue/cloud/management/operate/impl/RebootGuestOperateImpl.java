package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.GuestRebootRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.operate.bean.RebootGuestOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;

/**
 * 重启虚拟机
 * @author chenjun
 */
@Component
@Slf4j
public class RebootGuestOperateImpl extends AbstractOperate<RebootGuestOperate, ResultUtil<Void>> {

    public RebootGuestOperateImpl() {
        super(RebootGuestOperate.class);
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(RebootGuestOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        HostEntity host = hostMapper.selectById(guest.getLastHostId());
        if (guest.getStatus() == cn.roamblue.cloud.management.util.Constant.GuestStatus.REBOOT) {
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
        if (guest.getStatus() == cn.roamblue.cloud.management.util.Constant.GuestStatus.REBOOT) {
            if (guest.getLastHostId() > 0) {
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.RUNNING);
            } else {
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.STOP);
                guestMapper.updateById(guest);
            }
        }
    }
}