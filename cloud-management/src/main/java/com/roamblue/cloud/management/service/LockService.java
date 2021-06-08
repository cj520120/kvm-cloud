package com.roamblue.cloud.management.service;

import java.util.concurrent.TimeUnit;

public interface LockService {
    boolean tryLock(String key, int timeout, TimeUnit timeUnit);

    void lock(String key, int timeout, TimeUnit timeUnit);

    void unLock(String key);

    <T> T run(String key, LockCallable<T> callable, int timeout, TimeUnit timeUnit);

    boolean tryRun(String key, LockCallable<Void> callable, int timeout, TimeUnit timeUnit);

    @FunctionalInterface
    interface LockCallable<T> {
        T call();
    }
}

