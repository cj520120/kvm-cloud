package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.GuestNetworkMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.data.mapper.NetworkMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.ChangeGuestNetworkInterfaceOperate;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * 更改网卡挂载
 *
 * @author chenjun
 */
public class ChangeGuestNetworkInterfaceOperateImpl extends AbstractOperate<ChangeGuestNetworkInterfaceOperate, ResultUtil<Void>> {

    protected ChangeGuestNetworkInterfaceOperateImpl() {
        super(ChangeGuestNetworkInterfaceOperate.class);
    }

    @Override
    public void operate(ChangeGuestNetworkInterfaceOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestNetworkMapper guestNetworkMapper = SpringContextUtils.getBean(GuestNetworkMapper.class);
        NetworkMapper networkMapper = SpringContextUtils.getBean(NetworkMapper.class);
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getId());
        GuestEntity guest = guestMapper.selectById(guestNetwork.getGuestId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.ATTACH_DISK:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_DISK:
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
                    OsNic nic = OsNic.builder()
                            .mac(guestNetwork.getMac())
                            .driveType(guestNetwork.getDrive())
                            .name(guest.getName())
                            .deviceId(guestNetwork.getDeviceId())
                            .bridgeName(network.getBridge())
                            .build();
                    if (param.isAttach()) {
                        this.asyncCall(host, param, Constant.Command.GUEST_ATTACH_NIC, nic);
                    } else {
                        this.asyncCall(host, param, Constant.Command.GUEST_DETACH_NIC, nic);
                    }
                }
                break;
            default:
                OperateFactory.onCallback(param, "", GsonBuilderUtil.create().toJson(ResultUtil.success()));
                break;
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, ChangeGuestNetworkInterfaceOperate param, ResultUtil<Void> resultUtil) {
        if (!param.isAttach()) {
            GuestNetworkMapper guestNetworkMapper = SpringContextUtils.getBean(GuestNetworkMapper.class);
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getId());
            guestNetwork.setGuestId(0);
            guestNetworkMapper.updateById(guestNetwork);
        }
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.ATTACH_NIC:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_NIC:
                if (guest.getHostId() > 0) {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
                } else {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
                }
                guestMapper.updateById(guest);
                break;
        }
    }
}