package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.servcie.ConvertService;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenjun
 */
@Slf4j
public class BaseController {
    @Autowired
    protected ConvertService convertService;
    @Autowired
    private LockRunner lockRunner;

    public <T> T globalLockCall(LockRunner.LockAction<T> runnable) {
        return this.globalLockCall(RedisKeyUtil.getGlobalLockKey(), runnable);
    }

    public void globalLockCall(Runnable runnable) {
        this.globalLockCall(RedisKeyUtil.getGlobalLockKey(), () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T globalLockCall(String lockKey, LockRunner.LockAction<T> runnable) {
        return lockRunner.lockCall(lockKey, runnable);
    }
}
