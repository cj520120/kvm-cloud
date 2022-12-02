package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.GuestDestroyRequest;
import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.bean.GuestShutdownRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.operate.bean.StopGuestOperate;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class StopGuestOperateImpl extends AbstractOperate<StopGuestOperate, ResultUtil<Void>> {

    protected StopGuestOperateImpl() {
        super(StopGuestOperate.class);
    }

    @Override
    public void operate(StopGuestOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());

        HostEntity host = hostMapper.selectById(guest.getHostId());

        if (param.isForce()) {
            GuestShutdownRequest request = GuestShutdownRequest.builder().name(guest.getName()).build();
            this.asyncCall(host, param, Constant.Command.GUEST_SHUTDOWN, request);
        } else {
            GuestDestroyRequest request = GuestDestroyRequest.builder().name(guest.getName()).build();
            this.asyncCall(host, param, Constant.Command.GUEST_DESTROY, request);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, StopGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
        }
        guestMapper.updateById(guest);
    }
}