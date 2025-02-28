package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.operate.bean.StartGuestOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 启动虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StartGuestOperateImpl extends AbstractStartGuestOperateImpl<StartGuestOperate> {
    @Override
    public void operate(StartGuestOperate param) {
        super.start(param.getHostId(), param.getGuestId(), param);
    }

    @Override
    public void onFinish(StartGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        super.finish(param.getGuestId(), resultUtil);
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.START_GUEST;
    }

    @Override
    protected GuestQmaRequest buildQmaRequest(StartGuestOperate param, Map<String, Object> sysconfig) {
        return null;
    }
}
