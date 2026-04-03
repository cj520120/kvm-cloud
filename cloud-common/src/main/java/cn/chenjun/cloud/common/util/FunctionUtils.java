package cn.chenjun.cloud.common.util;

import java.util.concurrent.Callable;

public class FunctionUtils {
    public static <T> T ignoreErrorCall(Callable<T> callable ){
        try {
            return callable.call();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void ignoreRun(ThrowRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ignored) {

        }
    }

    @FunctionalInterface
    public interface ThrowRunnable {
        void run() throws Exception;
    }

}
