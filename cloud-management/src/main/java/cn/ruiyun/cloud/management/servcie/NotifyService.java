package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.management.websocket.cluster.WsCluster;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NotifyService {
    @Autowired
    @Lazy
    private WsCluster wsCluster;


    public <T> void publish(NotifyData<T> data) {
        try {
            this.wsCluster.onCloudEventNotify(data);
        } catch (Exception e) {
            log.error("发布消息失败:{}", data, e);
        }
    }


}
