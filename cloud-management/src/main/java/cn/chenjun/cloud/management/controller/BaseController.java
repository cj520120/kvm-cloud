package cn.chenjun.cloud.management.controller;

import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenjun
 */
public class BaseController {
    @Autowired
    private LockRunner lockRunner;

    public <T> T lockRun(LockRunner.LockAction<T> runnable) {
        return lockRunner.lockCall(RedisKeyUtil.GLOBAL_LOCK_KEY, runnable);
    }
}
