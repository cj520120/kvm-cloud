package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestInfoRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.GuestInfoOperate;
import cn.chenjun.cloud.management.servcie.VncService;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class GuestInfoOperateImpl extends AbstractOperate<GuestInfoOperate, ResultUtil<GuestInfo>> {


    @Autowired
    private VncService vncService;


    @Override
    public void operate(GuestInfoOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING) {

            HostEntity host = this.hostMapper.selectById(guest.getHostId());
            if (host == null || !Objects.equals(host.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机不存在或未就绪");
            }
            GuestInfoRequest request = GuestInfoRequest.builder()
                    .name(guest.getName())
                    .build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_INFO, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]不是运行状态:" + guest.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

    @Override
    public void onFinish(GuestInfoOperate param, ResultUtil<GuestInfo> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest != null && guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING) {
            this.allocateService.initHostAllocate();
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                this.vncService.updateVncPort(param.getGuestId(), resultUtil.getData().getVnc());
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.GUEST_INFO;
    }
}
