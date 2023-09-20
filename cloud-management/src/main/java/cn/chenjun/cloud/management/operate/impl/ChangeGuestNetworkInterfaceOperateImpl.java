package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.bean.OsNic;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(ChangeGuestNetworkInterfaceOperate param) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getGuestNetworkId());
        GuestEntity guest = guestMapper.selectById(guestNetwork.getGuestId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getHostId());
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
        } else {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(ChangeGuestNetworkInterfaceOperate param, ResultUtil<Void> resultUtil) {
        if (!param.isAttach()) {
            GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getGuestNetworkId());
            guestNetwork.setGuestId(0);
            guestNetwork.setDeviceId(0);
            guestNetworkMapper.updateById(guestNetwork);
        }
        this.notifyService.publish(NotifyMessage.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());

    }
}