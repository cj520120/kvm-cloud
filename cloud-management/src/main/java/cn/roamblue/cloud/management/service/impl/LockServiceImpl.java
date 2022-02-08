package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.data.entity.LockEntity;
import cn.roamblue.cloud.management.data.mapper.LockMapper;
import cn.roamblue.cloud.management.service.LockService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Service
public class LockServiceImpl extends AbstractService implements LockService {

    public final String lockUUID = UUID.randomUUID().toString();
    private ThreadLocal<Map<String, LockRef>> context = new ThreadLocal<>();
    @Autowired
    private LockMapper lockMapper;

    private void incrementLockRef(String key) {
        Map<String, LockRef> map = context.get();
        if (map == null) {
            map = new HashMap<>(4);
            context.set(map);
        }
        map.computeIfAbsent(key, k -> new LockRef()).incrementAndGet();
    }

    private int releaseLockRef(String key) {
        Map<String, LockRef> map = context.get();
        if (map == null) {
            return 0;
        }
        LockRef ref = map.get(key);
        int value = ref.decrementAndGet();
        if (value == 0) {
            map.remove(key);
        }
        if (map.isEmpty()) {
            context.remove();
        }
        return value;
    }

    @Override
    public boolean tryLock(String key, int timeout, TimeUnit timeUnit) {
        boolean isLock = false;
        LockEntity entity = lockMapper.findByName(key);
        if (entity == null) {
            try {
                entity = LockEntity.builder()
                        .lockName(key)
                        .lockThread(Thread.currentThread().getId())
                        .lockUuid(lockUUID)
                        .lockTime(new Date(System.currentTimeMillis()))
                        .lockTimeout(new Date(timeUnit.toMillis(timeout) + System.currentTimeMillis()))
                        .build();
                lockMapper.insert(entity);
                isLock = true;
            } catch (Exception err) {
                isLock = false;
            }
        } else {
            if (System.currentTimeMillis() > entity.getLockTimeout().getTime()) {
                QueryWrapper<LockEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("lock_name", entity.getLockName());
                wrapper.eq("id", entity.getId());
                wrapper.eq("lock_thread", entity.getLockThread());
                wrapper.eq("lock_uuid", entity.getLockUuid());
                wrapper.eq("lock_time", entity.getLockTime());
                wrapper.eq("lock_timeout", entity.getLockTimeout());
                entity.setLockUuid(lockUUID);
                entity.setLockThread(Thread.currentThread().getId());
                entity.setLockTime(new Date(System.currentTimeMillis()));
                entity.setLockTimeout(new Date(timeUnit.toMillis(timeout) + System.currentTimeMillis()));
                isLock = lockMapper.update(entity, wrapper) > 0;
            } else {
                isLock = lockUUID.equals(entity.getLockUuid()) && entity.getLockThread().equals(Thread.currentThread().getId());
            }
        }
        if (isLock) {
            this.incrementLockRef(key);
        }
        return isLock;
    }

    @Override
    public void lock(String key, int timeout, TimeUnit timeUnit) {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        while (System.currentTimeMillis() <= end) {
            if (tryLock(key, timeout, timeUnit)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        throw new CodeException(ErrorCode.LOCK_TIMEOUT, "获取锁超时");

    }

    @Override
    public void unLock(String key) {
        if (releaseLockRef(key) == 0) {
            QueryWrapper<LockEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("lock_name", key);
            wrapper.eq("lock_thread", Thread.currentThread().getId());
            wrapper.eq("lock_uuid", lockUUID);
            lockMapper.delete(wrapper);
        }
    }

    @Override
    public <T> T run(String key, LockCallable<T> callable, int timeout, TimeUnit timeUnit) {
        try {
            this.lock(key, timeout, timeUnit);
            return callable.call();
        } finally {
            unLock(key);
        }
    }

    @Override
    public boolean tryRun(String key, LockCallable<Void> callable, int timeout, TimeUnit timeUnit) {
        boolean bLock = false;
        try {
            bLock = this.tryLock(key, timeout, timeUnit);
            if (bLock) {
                callable.call();
            }
        } finally {
            if (bLock) {
                unLock(key);
            }
        }
        return bLock;
    }

    public static class LockRef {
        private int ref;

        public int decrementAndGet() {
            ref--;
            return Math.max(0, ref);
        }

        public int incrementAndGet() {
            ref++;
            return Math.max(0, ref);
        }
    }
}
