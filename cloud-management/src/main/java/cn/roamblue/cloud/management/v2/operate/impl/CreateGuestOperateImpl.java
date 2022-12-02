package cn.roamblue.cloud.management.v2.operate.impl;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.VolumeInfo;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.util.SpringContextUtils;
import cn.roamblue.cloud.management.v2.data.entity.GuestEntity;
import cn.roamblue.cloud.management.v2.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.v2.operate.OperateFactory;
import cn.roamblue.cloud.management.v2.operate.bean.CreateGuestOperate;
import cn.roamblue.cloud.management.v2.operate.bean.StartGuestOperate;
import cn.roamblue.cloud.management.v2.util.Constant;

import java.util.UUID;

/**
 * @author chenjun
 */
public class CreateGuestOperateImpl extends CreateVolumeOperateImpl<CreateGuestOperate> {
    public CreateGuestOperateImpl(){
        super(CreateGuestOperate.class);
    }


    @Override
    public void onCallback(String hostId, CreateGuestOperate param, ResultUtil<VolumeInfo> resultUtil) {
        super.onCallback(hostId, param, resultUtil);
        GuestMapper guestMapper= SpringContextUtils.getBean(GuestMapper.class);
        GuestEntity guest=guestMapper.selectById(param.getGuestId());
        if(guest.getStatus()== Constant.GuestStatus.CREATING) {
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setStatus(Constant.GuestStatus.STARTING);
                guestMapper.updateById(guest);
                StartGuestOperate operate= StartGuestOperate.builder().taskId(UUID.randomUUID().toString()).guestId(param.getGuestId()).build();
                OperateFactory.submitTask(operate);
            } else {
                guest.setStatus(Constant.GuestStatus.ERROR);
                guestMapper.updateById(guest);
            }
        }
    }
}
