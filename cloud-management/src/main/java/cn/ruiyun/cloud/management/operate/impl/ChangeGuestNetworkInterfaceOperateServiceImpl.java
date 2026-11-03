package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ChangeGuestInterfaceRequest;
import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.GuestNetworkEntity;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.data.entity.NetworkEntity;
import cn.ruiyun.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 更改网卡挂载
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestNetworkInterfaceOperateServiceImpl extends AbstractOsOperateService<ChangeGuestNetworkInterfaceOperate, ResultUtil<Void>> {


    @Override
    public void operate(ChangeGuestNetworkInterfaceOperate param) {
        GuestNetworkEntity guestNetwork = guestNetworkDao.findById(param.getGuestNetworkId());
        GuestEntity guest = guestDao.findById(guestNetwork.getAllocateId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostDao.findById(guest.getHostId());
            NetworkEntity network = networkDao.findById(guestNetwork.getNetworkId());
            Map<String, Object> systemConfig = this.loadGuestConfig(host.getHostId(), guest.getGuestId());
            ChangeGuestInterfaceRequest nic = ChangeGuestInterfaceRequest.builder()
                    .name(guest.getName())
                    .nic(this.buildInterfaceXml(network, guestNetwork, systemConfig))
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
            GuestNetworkEntity guestNetwork = guestNetworkDao.findById(param.getGuestNetworkId());
            if (guestNetwork != null) {
                this.allocateService.releaseNetwork(param.getGuestNetworkId());
            }
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());

    }

    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_NETWORK_INTERFACE;
    }
}
