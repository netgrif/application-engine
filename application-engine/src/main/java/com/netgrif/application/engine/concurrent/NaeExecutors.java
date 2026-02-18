package com.netgrif.application.engine.concurrent;

import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

// todo javadoc
public class NaeExecutors {

    /// @see Executors#newFixedThreadPool(int)
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(nThreads));
    }

    /// @see Executors#newWorkStealingPool(int)
    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new DelegatingSecurityContextExecutorService(Executors.newWorkStealingPool(parallelism));
    }

    /// @see Executors#newWorkStealingPool()
    public static ExecutorService newWorkStealingPool() {
        return new DelegatingSecurityContextExecutorService(Executors.newWorkStealingPool());
    }

    /// @see Executors#newFixedThreadPool(int, ThreadFactory)
    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    /// @see Executors#newSingleThreadExecutor()
    public static ExecutorService newSingleThreadExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newSingleThreadExecutor());
    }

    /// @see Executors#newSingleThreadExecutor(ThreadFactory)
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new DelegatingSecurityContextExecutorService(Executors.newSingleThreadExecutor(threadFactory));
    }

    /// @see Executors#newCachedThreadPool()
    public static ExecutorService newCachedThreadPool() {
        return new DelegatingSecurityContextExecutorService(Executors.newCachedThreadPool());
    }

    /// @see Executors#newCachedThreadPool(ThreadFactory)
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new DelegatingSecurityContextExecutorService(Executors.newCachedThreadPool(threadFactory));
    }

    /// @see Executors#newThreadPerTaskExecutor(ThreadFactory)
    public static ExecutorService newThreadPerTaskExecutor(ThreadFactory threadFactory) {
        return new DelegatingSecurityContextExecutorService(Executors.newThreadPerTaskExecutor(threadFactory));
    }

    /// @see Executors#newVirtualThreadPerTaskExecutor()
    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newVirtualThreadPerTaskExecutor());
    }

    /// @see Executors#newSingleThreadScheduledExecutor()
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatingSecurityContextScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
    }

    /// @see Executors#newSingleThreadScheduledExecutor(ThreadFactory)
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatingSecurityContextScheduledExecutorService(Executors.newSingleThreadScheduledExecutor(threadFactory));
    }

    /// @see Executors#newScheduledThreadPool(int)
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new DelegatingSecurityContextScheduledExecutorService(Executors.newScheduledThreadPool(corePoolSize));
    }

    /// @see Executors#newScheduledThreadPool(int, ThreadFactory)
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return new DelegatingSecurityContextScheduledExecutorService(Executors.newScheduledThreadPool(corePoolSize, threadFactory));
    }

}
