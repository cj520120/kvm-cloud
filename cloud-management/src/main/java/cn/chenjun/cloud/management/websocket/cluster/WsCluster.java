package cn.chenjun.cloud.management.websocket.cluster;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.WsSessionManager;
import cn.chenjun.cloud.management.websocket.cluster.process.ClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class WsCluster implements CommandLineRunner, MessageListener<NotifyData<?>> {
    @Autowired
    private RedissonClient redissonClient;
    private RTopic topic;
    @Autowired
    private WsSessionManager wsSessionManager;
    @Autowired
    private List<ClusterMessageProcess> notifyProcesses;

    @Override
    public void run(String... args) throws Exception {
        topic = redissonClient.getTopic(RedisKeyUtil.GLOBAL_NOTIFY_KET);
        topic.addListener(NotifyData.class, this);
    }

    @Override
    public void onMessage(CharSequence channel, NotifyData<?> msg) {
        RLock rLock = redissonClient.getReadWriteLock(RedisKeyUtil.GLOBAL_LOCK_KEY).readLock();
        try {
            rLock.lock(1, TimeUnit.MINUTES);
            ClusterMessageProcess process = notifyProcesses.stream().filter(p -> Objects.equals(p.getType(), msg.getType())).findFirst().orElse(new ClusterMessageProcess() {
                    @Override
                    public void process(NotifyData<?> msg) {
                        wsSessionManager.sendWebNotify(msg);
                    }

                    @Override
                    public int getType() {
                        return msg.getType();
                    }
                });
            process.process(msg);
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


    @EventListener
    public <T> void onCloudEventNotify(NotifyData<T> notifyData) {
        this.topic.publish(notifyData);
    }

}
