package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class ComponentCheckTask extends AbstractTask {

    @Autowired
    private NetworkMapper networkMapper;

    @Autowired
    private List<AbstractComponentService> componentServiceList;
    @Autowired
    private RedissonClient redissonClient;

    public ComponentCheckTask(@Autowired  List<AbstractComponentService> componentServiceLis){
        this.componentServiceList=componentServiceLis;
        this.componentServiceList.sort(Comparator.comparingInt(AbstractComponentService::order));
    }


    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY) {
                for (AbstractComponentService componentService : this.componentServiceList) {
                    RLock rLock = redissonClient.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY);
                    try {
                        rLock.lock(1, TimeUnit.MINUTES);
                        componentService.checkAndStart(network.getNetworkId());
                    } finally {
                        try {
                            if (rLock.isHeldByCurrentThread()) {
                                rLock.unlock();
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
        }

    }
}
