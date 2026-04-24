package cn.chenjun.cloud.management.task.runner;

import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.ComponentService;
import cn.chenjun.cloud.management.servcie.GuestService;
import cn.chenjun.cloud.management.util.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ClearUnlinkComponentGuestRunner extends AbstractRunner {
    @Autowired
    private ComponentService componentService;
    @Autowired
    private GuestService guestService;

    @Override
    protected void dispatch() throws Exception {
        List<GuestEntity> systemGuestList = this.componentService.listAllComponentGuest();
        if (systemGuestList.isEmpty()) {
            return;
        }
        List<ComponentEntity> componentList = this.componentService.listAllComponent();
        Set<Integer> componentIdSet = componentList.stream().map(ComponentEntity::getComponentId).collect(Collectors.toSet());
        for (GuestEntity systemGuest : systemGuestList) {

            if (!componentIdSet.contains(systemGuest.getOtherId())) {
                try {
                    switch (systemGuest.getStatus()) {
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.STARTING:
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.RUNNING:
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.REBOOT:
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.STOPPING:
                            log.info("停止未关联的组件虚拟机:{}", systemGuest.getName());
                            this.guestService.shutdown(systemGuest.getGuestId(), true);
                            break;
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.STOP:
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.ERROR:
                        case cn.chenjun.cloud.common.util.Constant.GuestStatus.DESTROY:
                            log.info("清理未关联的组件虚拟机:{}", systemGuest.getName());
                            this.guestService.destroyGuest(systemGuest.getGuestId());
                            break;
                    }
                } catch (Exception e) {
                    log.error("清理未关联的组件虚拟机失败:{}", systemGuest.getName(), e);
                }
            }
        }
    }

    @Override
    protected String getName() {
        return "清理未关联的组件虚拟机";
    }

    @Override
    public int getPeriodSeconds() {
        return configService.getConfig(ConfigKey.DEFAULT_TASK_COMPONENT_GUEST_CLEAR_TIMEOUT_SECOND);
    }
}
