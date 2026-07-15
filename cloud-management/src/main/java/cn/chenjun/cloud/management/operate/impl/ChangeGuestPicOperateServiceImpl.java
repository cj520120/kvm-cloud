package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ChangeGuestPciRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.HostPciDeviceEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestPicOperate;
import cn.chenjun.cloud.management.util.NotifyContextHolderUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * 更新虚拟机光驱
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestPicOperateServiceImpl extends AbstractOsOperateService<ChangeGuestPicOperate, ResultUtil<Void>> {

    @Override
    public void operate(ChangeGuestPicOperate param) {
        GuestEntity guest = guestDao.findById(param.getGuestId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostDao.findById(guest.getHostId());
            Map<String, Object> systemConfig = this.loadGuestConfig(guest.getHostId(), guest.getGuestId());
            HostPciDeviceEntity hostPciDeviceEntity = HostPciDeviceEntity.builder().domain(param.getDomain()).bus(param.getBus()).slot(param.getSlot()).func(param.getFunction()).build();
            String xml = this.buildHostPciXml(hostPciDeviceEntity, systemConfig);
            ChangeGuestPciRequest pci = ChangeGuestPciRequest.builder().name(guest.getName()).xml(xml).build();
            if (param.isAttach()) {
                this.asyncInvoker(host, param, Constant.Command.GUEST_ATTACH_PCI, pci);
            } else {
                this.asyncInvoker(host, param, Constant.Command.GUEST_DETACH_PCI, pci);
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
    public void onFinish(ChangeGuestPicOperate param, ResultUtil<Void> resultUtil) {
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST_PIC).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.CHANGE_GUEST_PIC;
    }
}
