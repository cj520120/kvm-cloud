package cn.ruiyun.cloud.management.operate.impl;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.bean.VolumeInfo;
import cn.ruiyun.cloud.common.core.operate.BaseOperateParam;
import cn.ruiyun.cloud.common.util.Constant;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.operate.bean.CreateGuestOperate;
import cn.ruiyun.cloud.management.operate.bean.StartGuestOperate;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestOperateServiceImpl extends CreateVolumeOperateServiceImpl<CreateGuestOperate> {


    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        GuestEntity guest = guestDao.findById(param.getGuestId());
        if (guest != null) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                if (param.isStart()) {
                    if (Objects.equals(Constant.GuestType.USER, guest.getType())) {
                        guest.setStatus(Constant.GuestStatus.STARTING);
                        guestDao.update(guest);
                        BaseOperateParam operateParam = StartGuestOperate.builder().id(UUID.randomUUID().toString()).title("启动系统主机[" + guest.getDescription() + "]").hostId(param.getHostId()).guestId(param.getGuestId()).build();
                        this.taskService.addTask(operateParam);
                    } else {
                        guest.setStatus(Constant.GuestStatus.STOP);
                        guestDao.update(guest);
                    }

                } else {
                    guest.setHostId(0);
                    guest.setLastHostId(0);
                    guest.setStatus(Constant.GuestStatus.STOP);
                    guestDao.update(guest);
                    this.allocateService.initHostAllocate();
                }
            } else {
                guest.setLastHostId(0);
                guest.setStatus(Constant.GuestStatus.ERROR);
                guestDao.update(guest);
                this.allocateService.initHostAllocate();
            }
            NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(guest.getNetworkId()).type(Constant.NotifyType.UPDATE_DNS).build());
        }
        NotifyContextHolderUtil.append(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }

    @Override
    public int getType() {
        return Constant.OperateType.CREATE_GUEST;
    }
}
