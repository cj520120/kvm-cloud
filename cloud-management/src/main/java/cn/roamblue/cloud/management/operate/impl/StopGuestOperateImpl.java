package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.GuestDestroyRequest;
import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.bean.GuestShutdownRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.GuestVncEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.operate.bean.StopGuestOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(StopGuestOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() != cn.roamblue.cloud.management.util.Constant.GuestStatus.STOPPING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "客户机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
        HostEntity host = hostMapper.selectById(guest.getLastHostId());
        if (param.isForce()) {
            GuestShutdownRequest request = GuestShutdownRequest.builder().name(guest.getName()).build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_SHUTDOWN, request);
        } else {
            GuestDestroyRequest request = GuestDestroyRequest.builder().name(guest.getName()).build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_DESTROY, request);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(StopGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.roamblue.cloud.management.util.Constant.GuestStatus.STOPPING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setHostId(0);
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.STOP);
                this.guestVncMapper.delete(new QueryWrapper<GuestVncEntity>().eq("guest_id", param.getGuestId()));
            } else {
                guest.setStatus(cn.roamblue.cloud.management.util.Constant.GuestStatus.RUNNING);
            }
            guestMapper.updateById(guest);
        }
    }
}