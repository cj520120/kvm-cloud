package com.roamblue.cloud.management.task;

import com.roamblue.cloud.management.service.ManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理端保活
 */
@Slf4j
@Component
public class ManagementKeepTask extends AbstractTask {
    @Autowired
    private ManagementService managementService;

    @Override
    protected int getInterval() {
        return 5000;
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
            log.error("management keep fail", e);
        }
    }
}
