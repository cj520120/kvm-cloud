package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.operate.bean.DestroyHostGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StopGuestOperate;
import cn.chenjun.cloud.management.operate.bean.SyncHostGuestOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class SyncHostGuestOperateImpl extends AbstractOperate<SyncHostGuestOperate, ResultUtil<List<GuestInfo>>> {

    public SyncHostGuestOperateImpl() {
        super(SyncHostGuestOperate.class);
    }


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(SyncHostGuestOperate param) {
        HostEntity host = hostMapper.selectById(param.getHostId());
        if (host == null) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.SERVER_ERROR, "主机不存在"));
        }
        if (!Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.SERVER_ERROR, "主机当前不在线"));
        }
        this.asyncInvoker(host, param, Constant.Command.ALL_GUEST_INFO, new HashMap<>(0));
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<GuestInfo>>>() {
        }.getType();
    }

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(SyncHostGuestOperate param, ResultUtil<List<GuestInfo>> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return;
        }
        this.allocateService.initHostAllocate();
        List<GuestInfo> guestList = resultUtil.getData();
        List<String> guestNames = guestList.stream().map(GuestInfo::getName).collect(Collectors.toList());
        if (!guestNames.isEmpty()) {
            List<GuestEntity> guestEntityList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().in("guest_name", guestNames));
            Map<String, GuestEntity> map = guestEntityList.stream().collect(Collectors.toMap(GuestEntity::getName, Function.identity()));
            for (String guestName : guestNames) {
                GuestEntity guest = map.get(guestName);
                if (guest == null || Objects.equals(cn.chenjun.cloud.management.util.Constant.GuestStatus.MIGRATE, guest.getStatus())) {
                    continue;
                }
                if (!Objects.equals(guest.getHostId(), param.getHostId()) || Objects.equals(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOP, guest.getStatus())) {
                    BaseOperateParam operate = DestroyHostGuestOperate.builder().hostId(param.getHostId()).name(guestName).title("同步停止主机:" + guest.getGuestId()).taskId(UUID.randomUUID().toString()).build();
                    this.operateTask.addTask(operate);
                }
            }
        }
        //检查运行的客户机不在当前主机
        {
            List<GuestEntity> guestEntityList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq("host_id", param.getHostId()));
            for (GuestEntity guest : guestEntityList) {
                if (Objects.equals(guest.getStatus(), cn.chenjun.cloud.management.util.Constant.GuestStatus.RUNNING)
                        && System.currentTimeMillis() - guest.getLastStartTime().getTime() > TimeUnit.MINUTES.toMillis(1)) {
                    //上次超过1分钟，则开始检测
                    if (!guestNames.contains(guest.getName())) {
                        //无效的主机状态，开始自动关机
                        guest.setStatus(cn.chenjun.cloud.management.util.Constant.GuestStatus.STOPPING);
                        this.guestMapper.updateById(guest);
                        BaseOperateParam operateParam = StopGuestOperate.builder().guestId(guest.getGuestId()).force(true)
                                .taskId(UUID.randomUUID().toString())
                                .title("强制同步客户机状态，开始关闭客户机[" + guest.getDescription() + "]").build();
                        this.operateTask.addTask(operateParam);
                    }
                }
            }
        }
    }
}