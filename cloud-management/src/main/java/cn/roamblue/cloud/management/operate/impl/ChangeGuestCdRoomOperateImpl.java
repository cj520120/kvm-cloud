package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.OsCdRoom;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.entity.TemplateVolumeEntity;
import cn.roamblue.cloud.management.operate.bean.ChangeGuestCdRoomOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * 更新虚拟机光驱
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestCdRoomOperateImpl<T extends ChangeGuestCdRoomOperate> extends AbstractOperate<T, ResultUtil<Void>> {

    public ChangeGuestCdRoomOperateImpl() {
        super((Class<T>) ChangeGuestCdRoomOperate.class);
    }
    public ChangeGuestCdRoomOperateImpl(Class<T> tClass){
        super(tClass);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(T param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getHostId());
            OsCdRoom cdRoom = OsCdRoom.builder().name(guest.getName()).build();
            if (guest.getCdRoom() > 0) {
                List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", guest.getCdRoom()));
                Collections.shuffle(templateVolumeList);
                if (templateVolumeList.size() > 0) {
                    TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
                    cdRoom.setPath(templateVolume.getPath());
                } else {
                    throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "光盘镜像未就绪");
                }
            }
            String command = Constant.Command.GUEST_DETACH_CD_ROOM;
            if (guest.getCdRoom() > 0) {
                command = Constant.Command.GUEST_ATTACH_CD_ROOM;
            }
            this.asyncInvoker(host, param, command, cdRoom);
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
    public void onFinish(T param, ResultUtil<Void> resultUtil) {

        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }
}