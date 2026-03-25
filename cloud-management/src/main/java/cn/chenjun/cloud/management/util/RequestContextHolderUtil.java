package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.util.FunctionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class RequestContextHolderUtil {
    public static final String REQUEST_ID = "RequestId";
    public static final String CURRENT_USER = "CurrentUser";
    private static final ThreadLocal<Map<String, Holder>> REQUEST_CONTEXT = new ThreadLocal<>();

    public static <T> T get(String key) {
        Map<String, Holder> context = REQUEST_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Request context is not initialized");
        }
        Holder<T> holder = (Holder<T>) context.get(key);
        if (holder == null) {
            return null;
        }
        return holder.getData();

    }

    public static <T> T get(String key, Callable<T> callable) {
        Map<String, Holder> context = REQUEST_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Request context is not initialized");
        }
        Holder<T> holder = (Holder<T>) context.get(key);
        if (holder == null) {
            holder = Holder.of(FunctionUtils.ignoreErrorCall(callable));
            context.put(key, holder);
        }
        return holder.getData();
    }

    public static void initContext() {
        if (REQUEST_CONTEXT.get() != null) {
            clearContext();
        }
        REQUEST_CONTEXT.set(new HashMap<>());
    }

    public static void clearContext() {
        REQUEST_CONTEXT.remove();
    }

    public static void put(String key, Object data) {
        Map<String, Holder> context = REQUEST_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("Request context is not initialized");
        }
        context.put(key, new Holder<>(data));
    }


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Holder<T> {
        private T data;

        public static <T> Holder<T> of(T data) {
            return new Holder<>(data);
        }
    }
}
