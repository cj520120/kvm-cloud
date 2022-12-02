package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.OsCdRoom;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.GsonBuilderUtil;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.ChangeGuestCdRoomOperate;
import com.google.gson.reflect.TypeToken;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

/**
 * 更新虚拟机光驱
 *
 * @author chenjun
 */
public class ChangeGuestCdRoomOperateImpl<T extends ChangeGuestCdRoomOperate> extends AbstractOperate<T, ResultUtil<Void>> {

    public ChangeGuestCdRoomOperateImpl() {
        super((Class<T>) ChangeGuestCdRoomOperate.class);
    }
    public ChangeGuestCdRoomOperateImpl(Class<T> tClass){
        super(tClass);
    }
    @Override
    public void operate(T param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.ATTACH_CD_ROOM:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_CD_ROOM:
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    OsCdRoom cdRoom = OsCdRoom.builder().name(guest.getCdRoom()).build();
                    String command = Constant.Command.GUEST_DETACH_CD_ROOM;
                    if (!StringUtils.isEmpty(guest.getCdRoom())) {
                        command = Constant.Command.GUEST_ATTACH_CD_ROOM;
                    }
                    this.asyncCall(host, param, command, cdRoom);
                } else {
                    this.onSubmitCallback(param.getTaskId(), ResultUtil.<Void>builder().build());
                }
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]状态不正确:" + guest.getStatus());
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, T param, ResultUtil<Void> resultUtil) {
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        switch (guest.getStatus()) {
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STARTING:
            case cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.DETACH_CD_ROOM:
                if (guest.getHostId() > 0) {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.RUNNING);
                } else {
                    guest.setStatus(cn.roamblue.cloud.management.v2.util.Constant.GuestStatus.STOP);
                }
                guestMapper.updateById(guest);
                break;
            default:
                break;
        }
    }
}