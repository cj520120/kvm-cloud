package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.VolumeInfo;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.CreateGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StartGuestOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestOperateImpl extends CreateVolumeOperateImpl<CreateGuestOperate> {
    public CreateGuestOperateImpl() {
        super(CreateGuestOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            if (param.isStart()) {
                guest.setStatus(Constant.GuestStatus.STARTING);
                guestMapper.updateById(guest);
                BaseOperateParam operateParam;
                if (Objects.equals(Constant.GuestType.SYSTEM, guest.getType())) {
                    operateParam = StartComponentGuestOperate.builder().taskId(UUID.randomUUID().toString()).title("启动系统主机[" + guest.getDescription() + "]").guestId(guest.getGuestId()).hostId(param.getHostId()).build();
                } else {
                    operateParam = StartGuestOperate.builder().taskId(UUID.randomUUID().toString()).title(param.getTitle()).hostId(param.getHostId()).guestId(param.getGuestId()).build();
                }
                this.operateTask.addTask(operateParam);
            } else {
                guest.setHostId(0);
                guest.setLastHostId(0);
                guest.setStatus(Constant.GuestStatus.STOP);
                guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
            }
        } else {
            guest.setLastHostId(0);
            guest.setStatus(Constant.GuestStatus.ERROR);
            guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(cn.chenjun.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

    }
}
