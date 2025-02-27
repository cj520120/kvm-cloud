package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ChangeGuestInterfaceRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestNetworkEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestNetworkInterfaceOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.DomainUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            List<ConfigQuery> queryList = new ArrayList<>();
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(host.getHostId()).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.GUEST).id(guest.getGuestId()).build());
            Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
            NetworkEntity network = networkMapper.selectById(guestNetwork.getNetworkId());
            String tpl = (String) sysconfig.get(cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_INTERFACE_TPL);
            String xml = DomainUtil.buildNetworkInterfaceXml(tpl, sysconfig, network, guestNetwork);
            ChangeGuestInterfaceRequest nic = ChangeGuestInterfaceRequest.builder()
                    .name(guest.getName())
                    .xml(xml)
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
