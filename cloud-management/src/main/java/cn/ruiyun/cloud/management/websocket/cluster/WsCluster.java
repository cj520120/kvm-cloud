package cn.ruiyun.cloud.management.websocket.cluster;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.util.RequestContextHolderUtil;
import cn.ruiyun.cloud.management.websocket.cluster.process.ClusterMessageProcess;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class WsCluster implements CommandLineRunner {

    @Autowired
    private PluginRegistry<ClusterMessageProcess, Integer> processPluginRegistry;
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 1;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int QUEUE_CAPACITY = 2000;
    private ThreadPoolExecutor executor = null;
    @Override
    public void run(String... args) throws Exception {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "local-task-worker-" + seq.getAndIncrement());
                t.setDaemon(false);
                return t;
            }
        };

        // 拒绝策略：这里用自定义，记录日志；也可以 DiscardPolicy / AbortPolicy / CallerRunsPolicy
        RejectedExecutionHandler handler = (r, pool) -> {
            log.error("本地任务池队列已满，拒绝执行任务，当前队列:{}", pool.getQueue().size());
        };

        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                threadFactory,
                handler
        );
        log.info("本地线程池初始化完成，core:{},max:{},queue:{}", CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
    }


    public void onMessage(NotifyData<?> msg) {
        try {
            RequestContextHolderUtil.initContext();
            Optional<ClusterMessageProcess> optional = this.processPluginRegistry.getPluginFor(msg.getType());
            ClusterMessageProcess process = optional.orElseThrow(() -> new CodeException(ErrorCode.SERVER_ERROR, "不支持的注册方式"));
            process.process(msg);
        } catch (Exception err) {
            log.error("process cluster msg fail.msg={}", msg, err);
        } finally {
            RequestContextHolderUtil.clearContext();
            NotifyContextHolderUtil.afterCompletion();
        }
    }


    @EventListener
    public <T> void onCloudEventNotify(NotifyData<T> notifyData) {
        this.executor.submit(() -> this.onMessage(notifyData));
    }

}
