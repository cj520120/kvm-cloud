package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.GuestRebootRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
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

public class RebootGuestOperateImpl extends AbstractOperate<RebootGuestOperate, ResultUtil<Void>> {

    protected RebootGuestOperateImpl() {
        super(RebootGuestOperate.class);
    }

    @Override
    public void operate(RebootGuestOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());

        HostEntity host = hostMapper.selectById(guest.getHostId());
        GuestRebootRequest request = GuestRebootRequest.builder().name(guest.getName()).build();
        this.asyncCall(host, param, Constant.Command.GUEST_REBOOT, request);
    }


    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, RebootGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            GuestEntity guest = guestMapper.selectById(param.getId());
            guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
            guestMapper.updateById(guest);
        }
    }
}