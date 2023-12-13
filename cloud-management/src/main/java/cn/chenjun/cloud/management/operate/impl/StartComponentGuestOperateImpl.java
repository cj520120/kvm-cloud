package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

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
    private PluginRegistry<ComponentProcess, Integer> componentPlugin;



    @Override
    protected GuestQmaRequest getStartQmaRequest(StartComponentGuestOperate param) {
        GuestEntity guest = this.guestMapper.selectById(param.getGuestId());
        ComponentEntity component = this.componentMapper.selectById(guest.getOtherId());
        if (component == null) {
            return null;
        }
        Optional<ComponentProcess> optional = componentPlugin.getPluginFor(component.getComponentType());
        return optional.map(process -> process.getStartQmaRequest(component, guest.getGuestId())).orElse(null);
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
