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
    private static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal<>();

    public static Context getCurrent() {
        Context context = THREAD_LOCAL.get();
        if (context == null) {
            context = new Context();
            THREAD_LOCAL.set(context);
        }
        return context;
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static void set(Context context) {
        THREAD_LOCAL.set(context);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Context {
        private LoginUserModel self;
    }
}
