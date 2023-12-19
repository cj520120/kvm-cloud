package cn.chenjun.cloud.agent.sock;

import java.nio.ByteBuffer;

/**
 * 客户端回调
 *
 * @author chenjun
 */
public interface NioCallback {
    /**
     * 连接关闭
     */
    void onClose();

    /**
     * 连接异常
     *
     * @param err 异常信息
     */
    void onError(Exception err);

    /**
     * 接收到数据
     *
     * @param buffer 数据包
     */
    void onData(ByteBuffer buffer);
}
