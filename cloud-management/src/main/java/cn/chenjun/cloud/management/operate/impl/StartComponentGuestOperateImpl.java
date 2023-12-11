package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StartComponentGuestOperateImpl extends StartGuestOperateImpl<StartComponentGuestOperate> {

    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private List<AbstractComponentService> componentServices;



    @Override
    protected GuestQmaRequest getStartQmaRequest(StartComponentGuestOperate param) {
        GuestEntity guest = this.guestMapper.selectById(param.getGuestId());
        Optional<AbstractComponentService> componentService = componentServices.stream().filter(t -> Objects.equals(t.getComponentType(), param.getComponentType())).findFirst();
        return componentService.map(abstractComponentService -> abstractComponentService.getStartQmaRequest(guest.getNetworkId(), guest.getGuestId())).orElse(null);
    }

    @Override
    public void onFinish(StartComponentGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        super.onFinish(param, resultUtil);

    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.START_COMPONENT_GUEST;
    }
}
