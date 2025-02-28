package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class NotifyService {

    private final RTopic topic;

    public NotifyService(@Autowired RedissonClient redissonClient) {
        topic = redissonClient.getTopic(RedisKeyUtil.GLOBAL_NOTIFY_KET);
    }

    public <T> void publish(NotifyData<T> notifyData) {
        this.topic.publish(notifyData);
    }
}
