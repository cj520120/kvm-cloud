package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.util.Constant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
public class ClearComponentGuestRunner extends AbstractRunner {

    @Autowired
    private GuestMapper guestMapper;

    @Autowired
    private ComponentMapper componentMapper;

    @Autowired
    private GuestService guestService;
    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(Constant.ConfigKey.DEFAULT_CLUSTER_TASK_CLEAR_COMPONENT_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq(GuestEntity.GUEST_TYPE, Constant.GuestType.COMPONENT));
        List<Integer> componentIds = guestList.stream().map(GuestEntity::getOtherId).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(componentIds)) {
            Map<Integer, ComponentEntity> componentMap = this.componentMapper.selectBatchIds(componentIds).stream().collect(Collectors.toMap(ComponentEntity::getComponentId, Function.identity()));
            guestList.stream().filter(guest -> !componentMap.containsKey(guest.getOtherId())).forEach(guest -> {
                switch (guest.getType()) {
                    case Constant.GuestStatus.RUNNING:
                        guestService.shutdown(guest.getGuestId(), true);
                        break;
                    case Constant.GuestStatus.STOP:
                    case Constant.GuestStatus.ERROR:
                        guestService.destroyGuest(guest.getGuestId());
                        break;
                    default:
                        break;
                }
            });
        }

    }

    @Override
    protected String getName() {
        return "清理未关联的组件虚拟机";
    }

    @Override
    protected boolean canRunning() {
        return true;
    }
}
