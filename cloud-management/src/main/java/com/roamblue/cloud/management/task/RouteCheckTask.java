package com.roamblue.cloud.management.task;

import com.roamblue.cloud.management.data.entity.ClusterEntity;
import com.roamblue.cloud.management.data.mapper.ClusterMapper;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.service.RouteService;
import com.roamblue.cloud.management.util.ClusterStatus;
import com.roamblue.cloud.management.util.LockKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RouteCheckTask extends AbstractTask {
    @Autowired
    private ClusterMapper clusterMapper;
    @Autowired
    private RouteService routeService;
    @Autowired
    private LockService lockService;

    @Override
    protected int getInterval() {
        return 5000;
    }

    @Override
    protected String getName() {
        return "RouteCheckTask";
    }

    @Override
    protected void call() {
        List<ClusterEntity> list = clusterMapper.selectAll();
        list.stream().filter(t -> t.getClusterStatus().equals(ClusterStatus.READY)).forEach(cluster -> {
            lockService.tryRun(LockKeyUtil.getRouteLock(cluster.getId()), () -> {
                routeService.start(cluster.getId());
                return null;
            }, 2, TimeUnit.MINUTES);
        });
    }

}
