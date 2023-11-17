package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * @author chenjun
 */
@Component
public class ComponentCheckTask extends AbstractTask {
    private static final int TASK_CHECK_TIME = 60;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private List<AbstractComponentService> componentServiceList;

    public ComponentCheckTask(@Autowired  List<AbstractComponentService> componentServiceLis){
        this.componentServiceList=componentServiceLis;
        this.componentServiceList.sort(Comparator.comparingInt(AbstractComponentService::order));
    }

    @Override
    protected int getPeriodSeconds() {
        return 5;
    }

    @Override
    protected void dispatch() throws Exception {

        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY) {
                String key = String.format(RedisKeyUtil.NETWORK_CHECK_KEY, network.getNetworkId());
                RBucket<Long> rBucket = redissonClient.getBucket(key);
                if (!rBucket.isExists() && rBucket.setIfAbsent(System.currentTimeMillis(), Duration.ofSeconds(TASK_CHECK_TIME))) {
                    for (AbstractComponentService componentService : this.componentServiceList) {
                        componentService.checkAndStart(network.getNetworkId());
                    }
                    rBucket.delete();
                }
            }
        }

    }
}
