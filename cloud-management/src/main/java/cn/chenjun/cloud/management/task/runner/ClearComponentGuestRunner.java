package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.GuestMapper;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.util.ConfigKey;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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
        return configService.getConfig(ConfigKey.DEFAULT_TASK_CLEAR_COMPONENT_TIMEOUT_SECOND);
    }

    @Override
    protected void dispatch() {
        List<GuestEntity> guestList = this.guestMapper.selectList(new QueryWrapper<GuestEntity>().eq(GuestEntity.GUEST_TYPE, Constant.GuestType.COMPONENT));
        if (!ObjectUtils.isEmpty(guestList)) {
            List<Integer> allComponentIds = this.componentMapper.selectList(new QueryWrapper<>()).stream().map(ComponentEntity::getComponentId).collect(Collectors.toList());
            for (GuestEntity guestEntity : guestList) {
                Integer componentId = guestEntity.getOtherId();
                boolean isClean = !allComponentIds.contains(componentId) || ObjectUtils.equals(guestEntity.getBindHostId(), 0);
                if (isClean) {
                    switch (guestEntity.getStatus()) {
                        case Constant.GuestStatus.STARTING:
                        case Constant.GuestStatus.RUNNING:
                            guestService.shutdown(guestEntity.getGuestId(), true);
                            break;
                        case Constant.GuestStatus.STOP:
                        case Constant.GuestStatus.ERROR:
                            guestService.destroyGuest(guestEntity.getGuestId());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

    }

    @Override
    protected String getName() {
        return "清理未关联的组件虚拟机";
    }
}
