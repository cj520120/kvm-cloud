package com.roamblue.cloud.management.service;

import java.util.concurrent.TimeUnit;

public interface LockService {
    /**
     * 获取锁
     *
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    boolean tryLock(String key, int timeout, TimeUnit timeUnit);

    /**
     * 获取锁
     *
     * @param key
     * @param timeout
     * @param timeUnit
     */
    void lock(String key, int timeout, TimeUnit timeUnit);

    /**
     * 解锁
     *
     * @param key
     */
    void unLock(String key);

    /**
     * 执行
     *
     * @param key
     * @param callable
     * @param timeout
     * @param timeUnit
     * @param <T>
     * @return
     */
    <T> T run(String key, LockCallable<T> callable, int timeout, TimeUnit timeUnit);

    /**
     * 尝试执行
     *
     * @param key
     * @param callable
     * @param timeout
     * @param timeUnit
     * @return
     */
    boolean tryRun(String key, LockCallable<Void> callable, int timeout, TimeUnit timeUnit);

    @FunctionalInterface
    interface LockCallable<T> {
        T call();
    }
}

