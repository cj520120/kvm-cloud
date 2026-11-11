package cn.ruiyun.cloud.management.servcie;

import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.ErrorCode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheService {
    private static final Cache<String, CacheItem> CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .initialCapacity(64)
            .concurrencyLevel(4)
            .recordStats()
            .build();

    public void put(String key, Object value) {
        CACHE.put(key, CacheItem.of(value, -1));
    }

    public void put(String key, Object value, long ttlMs) {
        CACHE.put(key, CacheItem.of(value, ttlMs));
    }

    public <T> T get(String key) {
        CacheItem<T> cacheItem = CACHE.getIfPresent(key);
        T value = null;
        if (cacheItem != null) {
            if (cacheItem.isExpired()) {
                CACHE.invalidate(key);
            } else {
                value = cacheItem.data;
            }
        }
        return value;
    }

    public <T> T getAndRemove(String key) {
        CacheItem<T> cacheItem = CACHE.getIfPresent(key);
        if (cacheItem != null) {
            CACHE.invalidate(key);
        }
        if (!cacheItem.isExpired()) {
            return cacheItem.data;
        }
        return null;
    }

    public void expire(String key, long ttlMs) {
        CacheItem cacheItem = CACHE.getIfPresent(key);
        if (cacheItem != null) {
            cacheItem.setExpireAt(System.currentTimeMillis() + ttlMs);
        }
    }

    public void delete(String key) {
        CACHE.invalidate(key);
    }

    public <T> T get(String key, CacheLoader<T> loader) {
        CacheItem<T> cacheItem = CACHE.getIfPresent(key);
        if (cacheItem != null && !cacheItem.isExpired()) {
            return cacheItem.data;
        }
        try {
            T value = loader.load();
            CACHE.put(key, CacheItem.of(value, -1));
            return value;
        } catch (CodeException e) {
            throw e;
        } catch (Exception e) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "获取数据失败", e);
        }
    }

    public boolean exists(String key) {
        CacheItem cacheItem = CACHE.getIfPresent(key);
        return cacheItem != null && !cacheItem.isExpired();
    }

    @FunctionalInterface
    public interface CacheLoader<T> {
        T load() throws Exception;
    }

    public static class CacheItem<T> {
        public final T data;
        public long expireAt; // 毫秒时间戳，-1=永不过期

        private CacheItem(T data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }

        public static <T> CacheItem<T> of(T data, long ttlMs) {
            long expire = ttlMs <= 0 ? -1 : System.currentTimeMillis() + ttlMs;
            return new CacheItem<>(data, expire);
        }

        public boolean isExpired() {
            return expireAt != -1 && System.currentTimeMillis() > expireAt;
        }

        public void setExpireAt(long expireAt) {
            this.expireAt = expireAt;
        }
    }
}
