package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestMigrateRequest;
import cn.chenjun.cloud.common.bean.NotifyInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.GuestInfoOperate;
import cn.chenjun.cloud.management.operate.bean.MigrateGuestOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class MigrateGuestOperateImpl extends AbstractOperate<MigrateGuestOperate, ResultUtil<Void>> {

    public MigrateGuestOperateImpl() {
        super(MigrateGuestOperate.class);
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(MigrateGuestOperate param) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.MIGRATE) {
            if (Objects.equals(param.getSourceHostId(), param.getToHostId())) {
                this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
                return;
            }
            HostEntity host = this.hostMapper.selectById(param.getToHostId());
            if (host == null || !Objects.equals(host.getStatus(), cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE)) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "主机不存在或未就绪");
            }
            HostEntity sourceHost = this.hostMapper.selectById(param.getSourceHostId());
            GuestMigrateRequest request = GuestMigrateRequest.builder()
                    .name(guest.getName())
                    .host(host.getHostIp())
                    .build();
            this.asyncInvoker(sourceHost, param, Constant.Command.GUEST_MIGRATE, request);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getName() + "]不是运行状态:" + guest.getStatus());
        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(MigrateGuestOperate param, ResultUtil<Void> resultUtil) {
        GuestEntity guest = guestMapper.selectById(param.getGuestId());
        if (guest.getStatus() == cn.chenjun.cloud.management.util.Constant.GuestStatus.MIGRATE) {
            guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING);
            if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                guest.setHostId(param.getToHostId());
                guest.setLastHostId(param.getToHostId());
            }else{
                guest.setHostId(param.getSourceHostId());
                guest.setLastHostId(param.getSourceHostId());
            }
            this.guestMapper.updateById(guest);
            this.allocateService.initHostAllocate();
            GuestInfoOperate operate=GuestInfoOperate.builder()
                    .taskId(UUID.randomUUID().toString())
                    .title("获取客户机VNC信息["+guest.getName()+"]")
                    .guestId(param.getGuestId())
                    .build();
            this.operateTask.addTask(operate);
        }
        this.notifyService.publish(NotifyInfo.builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
    }
}
