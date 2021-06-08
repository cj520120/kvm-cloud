package com.roamblue.cloud.management.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RequestContext {
    private static ThreadLocal<Context> threadLocal = new ThreadLocal<>();

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
        private int userId;
    }
}
