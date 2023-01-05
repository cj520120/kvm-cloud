package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestDestroyRequest;
import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostGuestOperate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.util.Objects;

/**
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
        if (host == null || !Objects.equals(cn.chenjun.cloud.management.util.Constant.HostStatus.ONLINE, host.getStatus())) {
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