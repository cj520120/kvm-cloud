package cn.chenjun.cloud.management.task;

import cn.chenjun.cloud.management.component.ComponentProcess;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.data.mapper.NetworkMapper;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

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
    private PluginRegistry<ComponentProcess, Integer> processPluginRegistry;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ComponentMapper componentMapper;


    @Override
    protected void dispatch() throws Exception {
        List<NetworkEntity> networkList = networkMapper.selectList(new QueryWrapper<>());
        for (NetworkEntity network : networkList) {
            if (network.getStatus() == Constant.NetworkStatus.READY) {
                List<ComponentEntity> components = this.componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq(ComponentEntity.NETWORK_ID, network.getNetworkId()));
                for (ComponentEntity component : components) {
                    RLock rLock = redissonClient.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY);
                    try {
                        rLock.lock(1, TimeUnit.MINUTES);
                        processPluginRegistry.getPluginFor(component.getComponentType()).ifPresent(process -> process.checkAndStart(network, component));
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
