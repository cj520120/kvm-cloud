package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LockRunner {
    @Autowired
    private RedissonClient redissonClient;
    public void lockRun(String key,  Runnable runnable){
        RLock lock =null;
        try {
            lock = redissonClient.getLock(key);
            lock.lock(30, TimeUnit.SECONDS);
            runnable.run();
        } catch (Exception err) {
            log.error("执行失败.lock-key:{}",key,err);
        } finally {
            try {
                if (lock!=null&&lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception err) {

            }
        }
    }
    public <T> T lockCall(String key,  LockAction<T> runnable){
        RLock lock =null;
        try {
            lock = redissonClient.getLock(key);
            lock.lock(30, TimeUnit.SECONDS);
            return runnable.run();
        } catch (Exception err) {
            log.error("执行失败.lock-key:{}",key,err);
            throw err;
        } finally {
            try {
                if (lock!=null&&lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception err) {

            }
        }
    }
    @FunctionalInterface
    public interface LockAction<T> {

        /**
         * 执行
         *
         * @return
         */
        T run();
    }
}
