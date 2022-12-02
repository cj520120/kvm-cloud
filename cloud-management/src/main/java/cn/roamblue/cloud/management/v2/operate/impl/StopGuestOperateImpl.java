package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.GuestDestroyRequest;
import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.bean.GuestShutdownRequest;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
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

/**
 * 停止虚拟机
 * @author chenjun
 */
public class StopGuestOperateImpl<T extends StopGuestOperate> extends AbstractOperate<T, ResultUtil<Void>> {

    public StopGuestOperateImpl() {
        super((Class<T>) StopGuestOperate.class);
    }
    public StopGuestOperateImpl(Class<T> tClass){
        super(tClass);
    }

    @Override
    public void operate(T param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        if (guest.getStatus() != cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOPPING) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "客户机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
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
    public void onCallback(String hostId, T param, ResultUtil<Void> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        if (guest.getStatus() == cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOPPING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setHostId(0);
                guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
            } else {
                guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
            }
            guestMapper.updateById(guest);
        }
    }
}