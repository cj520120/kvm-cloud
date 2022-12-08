package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.roamblue.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
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