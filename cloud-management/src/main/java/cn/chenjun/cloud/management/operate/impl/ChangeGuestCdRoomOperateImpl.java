package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.common.bean.OsCdRoom;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestCdRoomOperate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public ChangeGuestCdRoomOperateImpl(Class<T> tClass) {
        super(tClass);
    }

    @Override
    public void operate(T param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getHostId() > 0) {
            HostEntity host = hostMapper.selectById(guest.getHostId());
            OsCdRoom cdRoom = OsCdRoom.builder().name(guest.getName()).build();
            if (guest.getCdRoom() > 0) {
                List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq("template_id", guest.getCdRoom()));
                Collections.shuffle(templateVolumeList);
                if (!templateVolumeList.isEmpty()) {
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

        this.clusterService.publish(NotifyData.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }
}