package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NotifyService {

    private final RTopic topic;

    public NotifyService(@Autowired RedissonClient redissonClient) {
        topic = redissonClient.getTopic(RedisKeyUtil.getGlobalNotifyKey());
    }

    public <T> void publish(NotifyData<T> data) {
        try {
            this.topic.publish(data);
        }catch (Exception e){
            log.error("发布消息失败:{}",data,e);
        }
    }


}
