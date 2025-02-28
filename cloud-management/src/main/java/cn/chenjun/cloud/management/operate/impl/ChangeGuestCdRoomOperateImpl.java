package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ChangeGuestCdRoomRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestCdRoomOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 更新虚拟机光驱
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestCdRoomOperateImpl extends AbstractOsOperate<ChangeGuestCdRoomOperate, ResultUtil<Void>> {


    @Override
    public void operate(ChangeGuestCdRoomOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getHostId());
            List<ConfigQuery> queryList = new ArrayList<>();
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(host.getHostId()).build());
            queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.GUEST).id(guest.getGuestId()).build());
            Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
            String xml = this.getGuestCdRoom(guest, sysconfig);
            String command = Constant.Command.GUEST_DETACH_CD_ROOM;
            if (guest.getCdRoom() > 0) {
                command = Constant.Command.GUEST_ATTACH_CD_ROOM;
            }
            this.asyncInvoker(host, param, command, ChangeGuestCdRoomRequest.builder().name(guest.getName()).xml(xml).build());
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
    public void onFinish(ChangeGuestCdRoomOperate param, ResultUtil<Void> resultUtil) {

        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CHANGE_GUEST_CD_ROOM;
    }
}
