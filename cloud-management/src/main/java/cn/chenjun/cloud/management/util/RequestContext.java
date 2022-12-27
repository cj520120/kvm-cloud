package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.management.model.LoginUserModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
public class RequestContext {
    private static final ThreadLocal<Context> threadLocal = new ThreadLocal<>();

    public static Context getCurrent() {
        Context context = threadLocal.get();
        if (context == null) {
            context = new Context();
            threadLocal.set(context);
        }
        return context;
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static void set(Context context) {
        threadLocal.set(context);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Context {
        private LoginUserModel self;
    }
}
