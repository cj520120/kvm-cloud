package cn.ruiyun.cloud.management.controller;

import cn.ruiyun.cloud.management.servcie.ConvertService;
import cn.ruiyun.cloud.management.servcie.LockRunner;
import cn.ruiyun.cloud.management.util.CacheKeyUtil;
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
        return this.globalLockCall(CacheKeyUtil.getGlobalLockKey(), runnable);
    }

    public void globalLockCall(Runnable runnable) {
        this.globalLockCall(CacheKeyUtil.getGlobalLockKey(), () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T globalLockCall(String lockKey, LockRunner.LockAction<T> runnable) {
        return lockRunner.lockCall(lockKey, runnable);
    }
}
