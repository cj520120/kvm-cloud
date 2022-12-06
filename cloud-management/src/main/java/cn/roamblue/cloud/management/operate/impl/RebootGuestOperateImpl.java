package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.GuestRebootRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.operate.bean.RebootGuestOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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