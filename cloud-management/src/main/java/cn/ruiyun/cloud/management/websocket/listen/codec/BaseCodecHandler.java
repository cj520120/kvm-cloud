package cn.ruiyun.cloud.management.websocket.listen.codec;

import cn.ruiyun.cloud.common.socket.packet.WsMessage;

import javax.websocket.MessageHandler;
import java.io.Closeable;

public abstract class BaseCodecHandler<T extends WsMessage, V> implements MessageHandler.Partial<V>, Closeable {

}
