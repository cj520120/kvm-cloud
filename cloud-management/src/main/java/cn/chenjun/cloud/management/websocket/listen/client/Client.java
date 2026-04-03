package cn.chenjun.cloud.management.websocket.listen.client;

import cn.chenjun.cloud.common.event.EventHandler;
import cn.chenjun.cloud.common.socket.packet.WsMessage;
import cn.chenjun.cloud.management.websocket.listen.context.ConnectContext;

import java.io.Closeable;

public interface Client extends Closeable {


    void login(ConnectContext context);


    <T> void sendJsonPacket(WsMessage<T> data);

    void sendBinaryPacket(int command, byte[] data);

    void sendCommand(int command);

    ConnectContext getContext();

    long getLastActiveTime();

    void setLastActiveTime(long lastActiveTime);

    long getLastPingTime();

    void setLastPingTime(long lastPingTime);

    void registerOnClose(EventHandler<ConnectContext> onCloseHandler);

    void unregisterOnClose(EventHandler<ConnectContext> onCloseHandler);

    void registerOnLogin(EventHandler<ConnectContext> onLoginHandler);

    void unregisterOnLogin(EventHandler<ConnectContext> onLoginHandler);

    String getSessionId();

    Object putAttribute(String key, Object value);

    Object getAttribute(String key);

    int getType();
}
