package cn.chenjun.cloud.management.websocket.cluster;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.cluster.process.ClusterMessageProcess;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class WsCluster implements CommandLineRunner, MessageListener<NotifyData<?>> {
    @Autowired
    private RedissonClient redissonClient;
    private RTopic topic;
    @Autowired
    private PluginRegistry<ClusterMessageProcess, Integer> processPluginRegistry;

    @Override
    public void run(String... args) throws Exception {
        topic = redissonClient.getTopic(RedisKeyUtil.GLOBAL_NOTIFY_KET);
        topic.addListener(NotifyData.class, this);
    }

    @Override
    public void onMessage(CharSequence channel, NotifyData<?> msg) {
        RLock rLock = redissonClient.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY + "." + msg.getId());
        try {
            rLock.lock(1, TimeUnit.MINUTES);
            Optional<ClusterMessageProcess> optional = this.processPluginRegistry.getPluginFor(msg.getType());
            ClusterMessageProcess process = optional.orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "不支持的注册方式"));
            process.process(msg);
        } catch (Exception err) {
            log.error("process cluster msg fail.msg={}", msg, err);
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
