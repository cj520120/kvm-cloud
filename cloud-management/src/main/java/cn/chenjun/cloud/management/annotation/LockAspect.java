package cn.chenjun.cloud.management.annotation;

import cn.chenjun.cloud.management.util.RedisKeyUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
        RLock rLock = redisson.getLock(RedisKeyUtil.GLOBAL_LOCK_KEY);
        boolean isLocked = false;
        try {
            rLock.lock(1, TimeUnit.MINUTES);
            isLocked = true;
            return joinPoint.proceed();
        } finally {
            try {
                if (isLocked && rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (Exception ignored) {
            }
        }
    }

}
