package cn.chenjun.cloud.agent.sock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class NioSelector {
    private final ScheduledThreadPoolExecutor poolExecutor;
    private final Selector selector;
    private final Map<SocketChannel, NioClient> channelMap = new ConcurrentHashMap<>();
    private final ByteBuffer socketReceiveBuffer = ByteBuffer.allocate(10240);

    private boolean isRunning = true;

    public NioSelector() throws Exception {
        this.selector = Selector.open();
        this.poolExecutor = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("nio-executor-pool-%d").daemon(true).build());
        this.poolExecutor.scheduleAtFixedRate(this::run, 1000, 10, TimeUnit.MICROSECONDS);
    }


    private void run() {
        try {
            while (isRunning && selector.selectNow() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    try {
                        SelectionKey sk = iterator.next();
                        if (sk.isValid() && sk.isReadable()) {
                            SocketChannel channel = (SocketChannel) sk.channel();
                            try {
                                ((Buffer) socketReceiveBuffer).clear();
                                int len = channel.read(socketReceiveBuffer);
                                if (len < 0) {
                                    closeChannel(channel);
                                } else if (len > 0) {
                                    ((Buffer) socketReceiveBuffer).flip();
                                    NioClient client = channelMap.get(channel);
                                    if (client != null && client.getCallback() != null) {
                                        client.getCallback().onData(socketReceiveBuffer);
                                    }
                                }
                            }catch (Exception err){
                                closeChannel(channel);
                            }
                        }
                    } catch (Throwable err) {
                        log.error("处理nio事件出错", err);
                    } finally {
                        iterator.remove();
                    }
                }
            }
        } catch (Throwable err) {
            log.error("处理nio检测出错", err);
        }
    }
    private void closeChannel(SocketChannel channel) throws IOException {
        NioClient nioClient = channelMap.remove(channel);
        if (nioClient != null) {
            nioClient.close();
        }
        channel.close();
    }

    public NioClient createClient(String host, int port, NioCallback callback) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(host, port));
        NioClient client = new NioClient(socketChannel, callback);
        this.channelMap.put(socketChannel, client);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        return client;
    }

    @PreDestroy
    public void close() {
        if (this.isRunning) {
            this.isRunning = false;
            try {
                this.selector.close();
            } catch (Exception ignored) {

            }
            this.poolExecutor.shutdown();
        }
    }
}
