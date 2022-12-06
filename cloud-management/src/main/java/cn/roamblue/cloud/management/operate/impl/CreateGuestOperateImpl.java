package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.operate.bean.CreateGuestOperate;
import cn.roamblue.cloud.management.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class CreateGuestOperateImpl extends AbstractOperate<CreateGuestOperate, ResultUtil<Void>> {
    public CreateGuestOperateImpl() {
        super(CreateGuestOperate.class);
    }


    @Override
    public void operate(CreateGuestOperate param) {
        super.onSubmitFinishEvent(param.getTaskId(), ResultUtil.<Void>builder().build());
    }

    @Override
    public void onFinish(CreateGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (resultUtil.getCode() == ErrorCode.SUCCESS) {
            guest.setStatus(Constant.GuestStatus.STARTING);
            guestMapper.updateById(guest);
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
