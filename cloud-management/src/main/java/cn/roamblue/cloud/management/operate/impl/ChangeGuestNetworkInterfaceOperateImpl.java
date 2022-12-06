package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.GuestNetworkEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.NetworkEntity;
import cn.roamblue.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 更改网卡挂载
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestNetworkInterfaceOperateImpl extends AbstractOperate<ChangeGuestNetworkInterfaceOperate, ResultUtil<Void>> {

    public ChangeGuestNetworkInterfaceOperateImpl() {
        super(ChangeGuestNetworkInterfaceOperate.class);
    }

    @Override
    public void operate(ChangeGuestNetworkInterfaceOperate param) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getGuestNetworkId());
        GuestEntity guest = guestMapper.selectById(guestNetwork.getGuestId());
        if (guest.getLastHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getLastHostId());
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            OsNic nic = OsNic.builder()
                    .mac(guestNetwork.getMac())
                    .driveType(guestNetwork.getDriveType())
                    .name(guest.getName())
                    .deviceId(guestNetwork.getDeviceId())
                    .bridgeName(network.getBridge())
                    .build();
            if (param.isAttach()) {
                this.asyncInvoker(host, param, Constant.Command.GUEST_ATTACH_NIC, nic);
            } else {
                this.asyncInvoker(host, param, Constant.Command.GUEST_DETACH_NIC, nic);
            }
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

}