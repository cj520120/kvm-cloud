package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class EventService {
    @Autowired
    private ApplicationContext applicationContext;

    public <T> void publish(NotifyData<T> notifyData) {
        this.applicationContext.publishEvent(notifyData);
    }
}
