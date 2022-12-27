package cn.chenjun.cloud.management.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Aspect
@Component
public class LockAspect {
    @Autowired
    private RedissonClient redisson;

    @Pointcut("@annotation(cn.chenjun.cloud.management.annotation.Lock)")
    public void preLock() {
    }

    @Around("preLock()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Lock lock = signature.getMethod().getAnnotation(Lock.class);
        String key = lock.value();
        RReadWriteLock readWriteLock = redisson.getReadWriteLock(key);
        RLock rLock = lock.write() ? readWriteLock.writeLock() : readWriteLock.readLock();
        boolean isLocked = false;
        try {
            rLock.lock(lock.timeout(), lock.timeUnit());
            isLocked = true;
            return joinPoint.proceed();
        } finally {
            try {
                if (isLocked && rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

}
