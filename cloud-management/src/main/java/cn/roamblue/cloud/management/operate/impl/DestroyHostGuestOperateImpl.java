package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.GuestDestroyRequest;
import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.operate.bean.DestroyHostGuestOperate;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 停止虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyHostGuestOperateImpl extends AbstractOperate<DestroyHostGuestOperate, ResultUtil<Void>> {

    public DestroyHostGuestOperateImpl() {
        super(DestroyHostGuestOperate.class);
    }


    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY, write = false)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void operate(DestroyHostGuestOperate param) {
        HostEntity host = hostMapper.selectById(param.getHostId());
        if (host == null || !Objects.equals(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        } else {
            GuestDestroyRequest request = GuestDestroyRequest.builder().name(param.getName()).build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_DESTROY, request);
        }
    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }

}