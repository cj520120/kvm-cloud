package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestShutdownRequest;
import cn.chenjun.cloud.common.bean.NoneRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostGuestOperate;
import cn.chenjun.cloud.management.operate.bean.StopGuestOperate;
import cn.chenjun.cloud.management.operate.bean.SyncHostGuestOperate;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class SyncHostGuestOperateServiceImpl extends AbstractOperateService<SyncHostGuestOperate, ResultUtil<List<GuestInfo>>> {


    @Override
    public void operate(SyncHostGuestOperate param) {
        HostEntity host = hostDao.findById(param.getHostId());
        if (host == null) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.HOST_NOT_FOUND, "主机不存在"));
            return;

        }
        if (!Objects.equals(Constant.HostStatus.ONLINE, host.getStatus())) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.error(ErrorCode.HOST_NOT_READY, "主机当前不在线"));
        }
        this.asyncInvoker(host, param, Constant.Command.ALL_GUEST_INFO, NoneRequest.builder());
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<List<GuestInfo>>>() {
        }.getType();
    }

    @Override
    public void onFinish(SyncHostGuestOperate param, ResultUtil<List<GuestInfo>> resultUtil) {
        if (resultUtil.getCode() != ErrorCode.SUCCESS) {
            return;
        }
        this.allocateService.initHostAllocate();
        List<GuestInfo> guestList = resultUtil.getData();
        List<String> guestNames = guestList.stream().map(GuestInfo::getName).collect(Collectors.toList());
        if (!guestNames.isEmpty()) {
            List<GuestEntity> guestEntityList = this.guestDao.listByNames(guestNames);
            HostEntity host = hostDao.findById(param.getHostId());
            Map<String, GuestEntity> map = guestEntityList.stream().collect(Collectors.toMap(GuestEntity::getName, Function.identity()));
            for (String hostGuestName : guestNames) {
                GuestEntity guest = map.get(hostGuestName);
                if(guest==null){
                   if(Objects.equals(Constant.Enable.YES, this.configService.getConfig(ConfigKey.AUTO_DELETE_UNLINK_GUEST))) {
                        log.warn("主机客户机不存在，可能是已经被删除:{}:{},开始自动关机",host.getHostName(),hostGuestName);
                       GuestShutdownRequest request = GuestShutdownRequest.builder().name(hostGuestName).expireMillis(1).build();
                       StopGuestOperate operateParam = StopGuestOperate.builder().guestId(0).force(true).build();
                       this.asyncInvoker(host, operateParam, Constant.Command.GUEST_SHUTDOWN, request);
                   }else{
                       log.warn("主机客户机不存在，可能是已经被删除:{}:{},请手动清理",host.getHostName(),hostGuestName);
                   }
                }

            }
            for (String guestName : guestNames) {
                GuestEntity guest = map.get(guestName);
                if (guest == null || Objects.equals(Constant.GuestStatus.MIGRATE, guest.getStatus())) {
                    continue;
                }
                if (!Objects.equals(guest.getHostId(), param.getHostId()) || Objects.equals(Constant.GuestStatus.STOP, guest.getStatus())) {
                    BaseOperateParam operate = DestroyHostGuestOperate.builder().hostId(param.getHostId()).name(guestName).title("同步停止主机:" + guest.getGuestId()).id(UUID.randomUUID().toString()).build();
                    this.taskService.addTask(operate);
                }
            }
        }
        //检查运行的客户机不在当前主机
        {
            List<GuestEntity> guestEntityList = this.guestDao.listByHostId(param.getHostId());
            for (GuestEntity guest : guestEntityList) {
                if (Objects.equals(guest.getStatus(), Constant.GuestStatus.RUNNING)
                        && System.currentTimeMillis() - guest.getLastStartTime().getTime() > TimeUnit.MINUTES.toMillis(1)) {
                    //上次超过1分钟，则开始检测
                    if (!guestNames.contains(guest.getName())) {
                        //无效的主机状态，开始自动关机
                        guest.setStatus(Constant.GuestStatus.STOPPING);
                        this.guestDao.update(guest);
                        BaseOperateParam operateParam = StopGuestOperate.builder().guestId(guest.getGuestId()).force(true)
                                .id(UUID.randomUUID().toString())
                                .title("强制同步客户机状态，开始关闭客户机[" + guest.getDescription() + "]").build();
                        this.taskService.addTask(operateParam);
                    }
                }
            }
        }
    }

    @Override
    public int getType() {
        return Constant.OperateType.SYNC_HOST_GUEST;
    }
}
