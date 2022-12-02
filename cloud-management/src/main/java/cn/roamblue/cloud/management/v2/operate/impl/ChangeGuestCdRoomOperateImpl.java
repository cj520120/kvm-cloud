package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.OsCdRoom;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.entity.HostEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.data.mapper.HostMapper;
import cn.roamblue.cloud.management.v2.operate.bean.ChangeGuestCdRoomOperate;
import com.google.gson.reflect.TypeToken;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;

public class ChangeGuestCdRoomOperateImpl extends AbstractOperate<ChangeGuestCdRoomOperate, ResultUtil<Void>> {

    protected ChangeGuestCdRoomOperateImpl() {
        super(ChangeGuestCdRoomOperate.class);
    }

    @Override
    public void operate(ChangeGuestCdRoomOperate param) {
        HostMapper hostMapper = SpringContextUtils.getBean(HostMapper.class);
        GuestMapper guestMapper = SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest = guestMapper.selectById(param.getId());
        HostEntity host = hostMapper.selectById(guest.getHostId());
        OsCdRoom cdRoom = OsCdRoom.builder().name(guest.getCdRoom()).build();
        this.asyncCall(host, param, StringUtils.isEmpty(guest.getCdRoom()) ? Constant.Command.GUEST_DETACH_CD_ROOM : Constant.Command.GUEST_ATTACH_CD_ROOM, cdRoom);
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onCallback(String hostId, ChangeGuestCdRoomOperate param, ResultUtil<Void> resultUtil) {

    }
}