package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import cn.hutool.core.thread.ThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
    private LockRunner runner;

    public <T> void publish(NotifyData<T> notifyData) {
        this.executorService.submit(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS);
            runner.lockRun(RedisKeyUtil.GLOBAL_RW_LOCK_KET, false, () -> EventService.this.applicationContext.publishEvent(notifyData));
        });

    }
}
