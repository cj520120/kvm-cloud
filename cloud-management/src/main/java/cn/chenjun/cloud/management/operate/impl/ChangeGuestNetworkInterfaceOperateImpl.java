package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.OsNic;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
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


    @Override
    public void operate(ChangeGuestNetworkInterfaceOperate param) {
        GuestNetworkEntity guestNetwork = guestNetworkMapper.selectById(param.getGuestNetworkId());
        GuestEntity guest = guestMapper.selectById(guestNetwork.getAllocateId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getHostId());
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            OsNic nic = OsNic.builder()
                    .poolId(network.getPoolId())
                    .mac(guestNetwork.getMac())
                    .driveType(guestNetwork.getDriveType())
                    .name(guest.getName())
                    .deviceId(guestNetwork.getDeviceId())
                    .bridgeName(network.getBridge())
                    .bridgeType(Constant.NetworkBridgeType.fromBridgeType(network.getBridgeType()))
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
            if (guestNetwork != null) {
                guestNetwork.setAllocateId(0);
                guestNetwork.setDeviceId(0);
                guestNetworkMapper.updateById(guestNetwork);
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CHANGE_GUEST_NETWORK_INTERFACE;
    }
}
