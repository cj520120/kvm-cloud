package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * @author chenjun
 */
@Component
public class EventService {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ExecutorService executorService;
    @Autowired
    private LockRunner lockRunner;

    public <T> void publish(NotifyData<T> notifyData) {
        this.executorService.submit(() -> {
            lockRunner.lockRun(RedisKeyUtil.GLOBAL_LOCK_KEY,  () -> EventService.this.applicationContext.publishEvent(notifyData));
        });

    }
}
