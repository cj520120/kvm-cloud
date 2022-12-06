package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.operate.bean.CreateGuestOperate;
import cn.roamblue.cloud.management.operate.bean.StartGuestOperate;
import cn.roamblue.cloud.management.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestOperateImpl extends CreateVolumeOperateImpl<CreateGuestOperate> {
    public CreateGuestOperateImpl() {
        super(CreateGuestOperate.class);
    }


    @Override
    public void operate(CreateGuestOperate param) {
        super.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<VolumeInfo>builder().build());
    }

    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onFinish(param, resultUtil);
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            guest.setStatus(Constant.GuestStatus.STARTING);
            guestMapper.updateById(guest);
            StartGuestOperate guestOperate = StartGuestOperate.builder().guestId(param.getGuestId()).build();
            this.operateTask.addTask(guestOperate);
        } else {
            guest.setStatus(Constant.GuestStatus.ERROR);
            guestMapper.updateById(guest);
        }
    }

    @Override
    public Type getCallResultType() {
        return null;
    }
}
