package cn.chenjun.cloud.agent.sock;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author chenjun
 */
public class NioClient {

    @Getter
    private final NioCallback callback;
    private final SocketChannel socketChannel;
    private boolean isClosed = false;

    public NioClient(SocketChannel socketChannel, NioCallback callback) {
        this.callback = callback;
        this.socketChannel = socketChannel;


    }

    public synchronized void send(byte[] bytes) {

        try {
            ByteBuffer writeBuff = ByteBuffer.wrap(bytes);
            socketChannel.write(writeBuff);
        } catch (Exception err) {
            this.callback.onError(err);
            this.callback.onClose();
        }
    }

    public void close() {
        if (!isClosed) {
            this.isClosed = true;
            try {
                this.socketChannel.shutdownInput();
                this.socketChannel.shutdownOutput();
                this.socketChannel.close();
            } catch (Exception ignored) {

            }
            this.callback.onClose();
        }
    }
}
