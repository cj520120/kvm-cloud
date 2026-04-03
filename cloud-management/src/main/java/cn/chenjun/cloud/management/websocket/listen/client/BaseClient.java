package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.event.EventListener;
import cn.chenjun.cloud.common.util.FunctionUtils;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class BaseClient implements Client {
    public final EventListener<ConnectContext> onClose = new EventListener<>();
    public final EventListener<ConnectContext> onLogin = new EventListener<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final int socketType;
    private final String sessionId;
    protected ConnectContext context;
    private long lastActiveTime;
    private long lastPingTime;

    public BaseClient(int socketType) {
        this.socketType = socketType;
        this.sessionId = UUID.randomUUID().toString();
        this.lastActiveTime = System.currentTimeMillis();
        this.lastPingTime = System.currentTimeMillis();
    }

    @Override
    public Object putAttribute(String key, Object value) {
        return this.attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public int getType() {
        return this.socketType;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public ConnectContext getContext() {
        return this.context;
    }

    public void setContext(ConnectContext context) {
        this.context = context;
    }

    @Override
    public void registerOnClose(EventHandler<ConnectContext> onCloseHandler) {
        this.onClose.addEvent(onCloseHandler);
    }

    @Override
    public void unregisterOnClose(EventHandler<ConnectContext> onCloseHandler) {
        this.onClose.removeEvent(onCloseHandler);
    }

    @Override
    public void registerOnLogin(EventHandler<ConnectContext> onLoginHandler) {
        this.onLogin.addEvent(onLoginHandler);

    }

    @Override
    public void unregisterOnLogin(EventHandler<ConnectContext> onLoginHandler) {
        this.onLogin.removeEvent(onLoginHandler);
    }

    @Override
    public long getLastActiveTime() {
        return this.lastActiveTime;
    }

    @Override
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    @Override
    public long getLastPingTime() {
        return lastPingTime;
    }

    @Override
    public void setLastPingTime(long lastPingTime) {
        this.lastPingTime = lastPingTime;
    }

    @SneakyThrows
    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            FunctionUtils.ignoreRun(this::release);
            FunctionUtils.ignoreRun(() -> this.onClose.fire(this, this.context));
            this.onClose.clear();
            this.onLogin.clear();
        }
    }

    protected abstract void release() throws Exception;

    @Override
    public void login(ConnectContext context) {
        this.context = context;
        try {
            this.onLogin.fire(this, this.context);
        } catch (Exception err) {
            log.error("websocket login error", err);
        }
    }


}
