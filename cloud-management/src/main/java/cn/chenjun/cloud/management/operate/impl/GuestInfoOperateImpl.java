package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestInfoRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.GuestInfoOperate;
import cn.chenjun.cloud.management.util.GuestExternNames;
import cn.chenjun.cloud.management.util.GuestExternUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class GuestInfoOperateImpl extends AbstractOperate<GuestInfoOperate, ResultUtil<GuestInfo>> {


    @Override
    public void operate(GuestInfoOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == Constant.GuestStatus.RUNNING) {

            HostEntity host = this.hostMapper.selectById(guest.getHostId());
            if (host == null || !Objects.equals(host.getStatus(), Constant.HostStatus.ONLINE)) {
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
        if (guest != null && guest.getStatus() == Constant.GuestStatus.RUNNING) {
            this.allocateService.initHostAllocate();
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {

                Map<String, Map<String, String>> extern = GsonBuilderUtil.create().fromJson(guest.getExtern(), new TypeToken<Map<String, Map<String, String>>>() {
                }.getType());
                Map<String, String> vncMap = extern.computeIfAbsent(GuestExternNames.VNC, k -> GuestExternUtil.buildVncParam(guest, "", ""));
                HostEntity host = this.hostMapper.selectById(guest.getHostId());
                vncMap.put(GuestExternNames.VncNames.HOST, host.getHostIp());
                if (!Objects.equals(vncMap.get(GuestExternNames.VncNames.PORT), String.valueOf(resultUtil.getData().getVnc()))) {
                    vncMap.put(GuestExternNames.VncNames.PORT, String.valueOf(resultUtil.getData().getVnc()));
                    guest.setExtern(GsonBuilderUtil.create().toJson(extern));
                    this.guestMapper.updateById(guest);
                }
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.GUEST_INFO;
    }
}
