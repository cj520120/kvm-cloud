package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LockRunner {

    public void lockRun(String key, Runnable runnable) {
        synchronized (key.intern()) {
            try {
                runnable.run();
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("执行失败.lock-key:{}", key, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
        }
    }

    public <T> T lockCall(String key, LockAction<T> runnable) {
        synchronized (key.intern()) {
            try {
            return runnable.run();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("执行失败.lock-key:{}", key, err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {

        /**
         * 执行
         *
         * @return 结果
         */
        T run() throws Exception;
    }
}
