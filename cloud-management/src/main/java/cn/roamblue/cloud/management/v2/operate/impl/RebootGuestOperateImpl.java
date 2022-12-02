package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.GuestRebootRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.operate.bean.RebootGuestOperate;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 重启虚拟机
 * @author chenjun
 */
public class RebootGuestOperateImpl extends AbstractOperate<RebootGuestOperate, ResultUtil<Void>> {

    public RebootGuestOperateImpl() {
        super(RebootGuestOperate.class);
    }

    @Override
    public void operate(RebootGuestOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());

        HostEntity host = hostMapper.selectById(guest.getHostId());
        if (guest.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.REBOOT) {
            GuestRebootRequest request = GuestRebootRequest.builder().name(guest.getName()).build();
            this.asyncCall(host, param, Constant.Command.GUEST_REBOOT, request);
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
    public void onCallback(String hostId, RebootGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        if (guest.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.REBOOT) {
            if (guest.getHostId() > 0) {
                guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
            } else {
                guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
                guestMapper.updateById(guest);
            }
        }
    }
}