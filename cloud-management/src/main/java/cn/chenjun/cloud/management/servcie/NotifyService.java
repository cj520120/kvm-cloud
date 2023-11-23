package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.common.bean.NotifyMessage;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.model.DnsModel;
import cn.chenjun.cloud.management.model.VncModel;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.util.SpringContextUtils;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class NotifyService implements CommandLineRunner, MessageListener<NotifyMessage<?>> {
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
    public void onMessage(CharSequence channel, NotifyMessage<?> msg) {
        RLock rLock = redissonClient.getReadWriteLock(RedisKeyUtil.GLOBAL_LOCK_KEY).readLock();
        try {
            if (rLock.tryLock(1, TimeUnit.MINUTES)) {
                switch (msg.getType()) {
                    case Constant.NotifyType.COMPONENT_UPDATE_VNC: {
                        List<VncModel> vncModelList = (List<VncModel>) msg.getData();
                        if (vncModelList == null) {
                            vncModelList = SpringContextUtils.getBean(VncService.class).listVncByNetworkId(msg.getId());
                        }
                        NotifyMessage<List<VncModel>> sendMsg = NotifyMessage.<List<VncModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_VNC).data(vncModelList).build();
                        ComponentNotify.sendNotify(msg.getId(), sendMsg);
                    }
                    break;
                    case Constant.NotifyType.COMPONENT_UPDATE_DNS: {
                        List<DnsModel> dnsModelList = (List<DnsModel>) msg.getData();
                        if (dnsModelList == null) {
                            dnsModelList = SpringContextUtils.getBean(DnsService.class).listLocalNetworkDns(msg.getId());
                        }
                        NotifyMessage<List<DnsModel>> sendMsg = NotifyMessage.<List<DnsModel>>builder().type(Constant.NotifyType.COMPONENT_UPDATE_DNS).data(dnsModelList).build();
                        ComponentNotify.sendNotify(msg.getId(), sendMsg);
                    }
                    break;
                    default:
                        WebNotify.sendNotify(msg);
                        break;
                }

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
