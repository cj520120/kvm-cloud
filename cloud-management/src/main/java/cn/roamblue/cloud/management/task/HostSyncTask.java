package cn.roamblue.cloud.management.task;

import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.common.bean.HostInfo;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.gson.GsonBuilderUtil;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.management.data.entity.HostEntity;
import cn.roamblue.cloud.management.data.mapper.HostMapper;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HostSyncTask extends AbstractTask {
    private final int TASK_CHECK_TIME = 30;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HostMapper hostMapper;


    @Override
    protected void dispatch() {
        RBucket<Long> rBucket = redissonClient.getBucket(RedisKeyUtil.HOST_SYNC_KEY);
        if (rBucket.isExists()) {
            return;
        }
        if (rBucket.trySet(System.currentTimeMillis(), TASK_CHECK_TIME, TimeUnit.SECONDS)) {
            List<HostEntity> hostList = hostMapper.selectList(new QueryWrapper<>());
            for (HostEntity host : hostList) {
                switch (host.getStatus()) {
                    case cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE:
                    case cn.roamblue.cloud.management.util.Constant.HostStatus.OFFLINE:
                        Map<String, Object> map = new HashMap<>(3);
                        map.put("command", Constant.Command.HOST_INFO);
                        map.put("taskId", UUID.randomUUID().toString());
                        map.put("data", "{}");
                        HostInfo hostInfo = null;
                        try {
                            String uri = String.format("%s/api/operate", host.getUri());
                            String response = HttpUtil.post(uri, map);
                            ResultUtil<HostInfo> resultUtil = GsonBuilderUtil.create().fromJson(response, new TypeToken<ResultUtil<HostInfo>>() {
                            }.getType());
                                hostInfo = resultUtil.getData();

                            } catch (Exception err) {
                               log.error("检测主机[{}]状态失败.",host.getDisplayName(),err);
                            }
                            HostEntity updateHost= HostEntity.builder().hostId(host.getHostId()).build();
                            if (hostInfo != null) {
                                updateHost.setHostName(hostInfo.getHostName());
                                updateHost.setTotalCpu(hostInfo.getCpu());
                                updateHost.setTotalMemory(hostInfo.getMemory());
                                updateHost.setEmulator(hostInfo.getEmulator());
                                updateHost.setArch(hostInfo.getArch());
                                updateHost.setHypervisor(hostInfo.getHypervisor());
                                updateHost.setStatus(cn.roamblue.cloud.management.util.Constant.HostStatus.ONLINE);
                            } else {
                                updateHost.setStatus(cn.roamblue.cloud.management.util.Constant.HostStatus.OFFLINE);
                            }
                        hostMapper.updateById(host);
                        break;
                }
            }
        }
    }
}
