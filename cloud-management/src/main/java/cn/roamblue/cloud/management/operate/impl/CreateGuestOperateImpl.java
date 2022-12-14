package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.NotifyInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.operate.bean.CreateGuestOperate;
import cn.roamblue.cloud.management.operate.bean.StartGuestOperate;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY,write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            if(param.isStart()) {
                guest.setStatus(Constant.GuestStatus.STARTING);
                guestMapper.updateById(guest);
                StartGuestOperate guestOperate = StartGuestOperate.builder().taskId(UUID.randomUUID().toString()).title(param.getTitle()).hostId(param.getHostId()).guestId(param.getGuestId()).build();
                this.operateTask.addTask(guestOperate);
            }else{
                guest.setHostId(0);
                guest.setLastHostId(0);
                guest.setStatus(Constant.GuestStatus.STOP);
                guestMapper.updateById(guest);
                this.allocateService.initHostAllocate();
            }
        } else {
            guest.setStatus(Constant.GuestStatus.ERROR);
            guestMapper.updateById(guest);
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(cn.roamblue.cloud.common.util.Constant.NotifyType.UPDATE_GUEST).build());

    }
}
