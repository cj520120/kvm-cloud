package cn.chenjun.cloud.management.websocket.cluster;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.WsManager;
import cn.chenjun.cloud.management.websocket.cluster.process.ClusterProcess;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class ClusterService implements CommandLineRunner, MessageListener<NotifyData<?>> {
    @Autowired
    private RedissonClient redissonClient;
    private RTopic topic;
    @Autowired
    private WsManager wsManager;
    @Autowired
    private List<ClusterProcess> clusterProcesses;

    @Override
    public void run(String... args) throws Exception {
        topic = redissonClient.getTopic(RedisKeyUtil.GLOBAL_NOTIFY_KET);
        topic.addListener(NotifyData.class, this);
    }

    @Override
    public void onMessage(CharSequence channel, NotifyData<?> msg) {
        RLock rLock = redissonClient.getReadWriteLock(RedisKeyUtil.GLOBAL_LOCK_KEY).readLock();
        try {
            if (rLock.tryLock(1, TimeUnit.MINUTES)) {
                ClusterProcess process = clusterProcesses.stream().filter(p -> Objects.equals(p.getType(), msg.getType())).findFirst().orElse(new ClusterProcess() {
                    @Override
                    public void process(NotifyData<?> msg) {
                        wsManager.sendWebNotify(msg);
                    }

                    @Override
                    public int getType() {
                        return msg.getType();
                    }
                });
                process.process(msg);

            }
        } catch (Exception ignored) {

        } finally {
            try {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (Exception ignored) {

            }
        }
    }

    public void publish(NotifyData notify) {
        this.topic.publish(notify);
    }


}
