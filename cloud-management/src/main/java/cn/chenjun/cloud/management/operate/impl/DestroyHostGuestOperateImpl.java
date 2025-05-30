package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestDestroyRequest;
import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.operate.bean.DestroyHostGuestOperate;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class DestroyHostGuestOperateImpl extends AbstractOperate<DestroyHostGuestOperate, ResultUtil<Void>> {


    @Override
    public void operate(DestroyHostGuestOperate param) {
        HostEntity host = hostMapper.selectById(param.getHostId());
        if (host == null || !Objects.equals(Constant.HostStatus.ONLINE, host.getStatus())) {
            this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
        } else {
            GuestDestroyRequest request = GuestDestroyRequest.builder().name(param.getName()).build();
            this.asyncInvoker(host, param, Constant.Command.GUEST_DESTROY, request);
        }
    }

    @Override
    public void onFinish(DestroyHostGuestOperate param, ResultUtil<Void> resultUtil) {

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<GuestInfo>>() {
        }.getType();
    }


    @Override
    public int getType() {
        return Constant.OperateType.DESTROY_HOST_GUEST;
    }
}
