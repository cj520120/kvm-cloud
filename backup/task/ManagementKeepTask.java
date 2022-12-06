package cn.roamblue.cloud.management.task;

import cn.roamblue.cloud.management.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理端保活
 *
 * @author chenjun
 */
@Slf4j
@Component
public class ManagementKeepTask extends AbstractTask {
    @Autowired
    private ManagementService managementService;

    @Override
    protected int getInterval() {
        return this.config.getManagerKeepInterval();
    }

    @Override
    protected String getName() {
        return "ManagementKeepTask";
    }

    @Override
    protected void call() {

    }

    @Override
    public void schedule() {
        try {
            managementService.keep();
        } catch (Exception e) {
            log.error("管理端心跳失败", e);
        }
    }
}
