package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class NotifyService implements CommandLineRunner, MessageListener<NotifyMessage> {
    private static final Set<Session> CLIENT_SESSIONS = Collections.synchronizedSet(new HashSet<>());
    @Autowired
    private RedissonClient redissonClient;
    private RTopic topic;

    @Override
    public void run(String... args) throws Exception {
        topic = redissonClient.getTopic(RedisKeyUtil.GLOBAL_NOTIFY_KET);
        topic.addListener(NotifyMessage.class, this);
    }

    @Override
    public void onMessage(CharSequence channel, NotifyMessage msg) {
        RLock rLock = redissonClient.getReadWriteLock(RedisKeyUtil.GLOBAL_LOCK_KEY).readLock();
        try {
            if (rLock.tryLock(1, TimeUnit.MINUTES)) {
                WebSocketServerOne.sendNotify(msg);
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

    public void publish(NotifyMessage notify) {
        this.topic.publish(notify);
    }


}
