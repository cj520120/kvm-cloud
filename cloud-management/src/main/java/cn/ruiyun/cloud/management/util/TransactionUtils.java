package cn.ruiyun.cloud.management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TransactionUtils {

    /**
     * 事务提交之后执行；无事务直接同步执行
     */
    public static void doAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionRunner runner = getOrRegisterRunner();
            runner.addAfterCommitToNormal(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 事务提交之后【优先执行】的任务；无事务直接同步执行
     */
    public static void doAfterCommitHead(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionRunner runner = getOrRegisterRunner();
            runner.addAfterCommitToHead(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 事务提交之后【尾部最后执行】的任务；无事务直接同步执行
     */
    public static void doAfterCommitTail(Runnable runnable) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionRunner runner = getOrRegisterRunner();
            runner.addAfterCommitToTail(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 获取已经注册的Runner，没有则新建并注册到当前事务上下文
     */
    private static TransactionRunner getOrRegisterRunner() {
        List<TransactionSynchronization> syncList = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization sync : syncList) {
            if (sync instanceof TransactionRunner) {
                return (TransactionRunner) sync;
            }
        }
        TransactionRunner runner = new TransactionRunner();
        // 关键：注册到当前事务同步器，否则回调不会触发
        TransactionSynchronizationManager.registerSynchronization(runner);
        return runner;
    }

    private static final class TransactionRunner implements TransactionSynchronization {
        /**
         * 头部优先任务，最先执行
         */
        private final List<Runnable> headTasks = new ArrayList<>();
        /**
         * 普通afterCommit任务，顺序执行
         */
        private final List<Runnable> normalTasks = new ArrayList<>();
        /**
         * 尾部任务，最后执行
         */
        private final List<Runnable> tailTasks = new ArrayList<>();
        private final Set<String> invalidatedCacheKeys = new LinkedHashSet<>();

        public void addAfterCommitToHead(Runnable runnable) {
            headTasks.add(runnable);
        }

        public void addAfterCommitToNormal(Runnable runnable) {
            normalTasks.add(runnable);
        }

        public void addAfterCommitToTail(Runnable runnable) {
            tailTasks.add(runnable);
        }

        public void addCacheInvalidation(String key) {
            invalidatedCacheKeys.add(key);
        }

        public List<String> cacheInvalidations() {
            return new ArrayList<>(invalidatedCacheKeys);
        }

        @Override
        public void afterCommit() {
            // 执行顺序：head → normal → tail
            for (Runnable r : headTasks) {
                call(r);
            }
            for (Runnable r : normalTasks) {
                call(r);
            }
            for (Runnable r : tailTasks) {
                call(r);
            }
        }

        private void call(Runnable r) {
            try {
                r.run();
            } catch (Exception e) {
                log.error("执行事务任务异常", e);
            }
        }

        /**
         * 无论提交成功/回滚，都清理任务，防止ThreadLocal泄漏，任务残留
         */
        @Override
        public void afterCompletion(int status) {
            headTasks.clear();
            normalTasks.clear();
            tailTasks.clear();
            invalidatedCacheKeys.clear();
        }

        @Override
        public void flush() {
        }

        @Override
        public void beforeCommit(boolean readOnly) {
        }

        @Override
        public void beforeCompletion() {
        }
    }
}
