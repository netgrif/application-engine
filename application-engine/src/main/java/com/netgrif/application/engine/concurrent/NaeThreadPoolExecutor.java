package com.netgrif.application.engine.concurrent;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

import java.util.concurrent.*;

// todo javadoc
public class NaeThreadPoolExecutor extends ThreadPoolExecutor {

    /// @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)
    public NaeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit,
                                 @NotNull BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /// @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue, ThreadFactory)
    public NaeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                 BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /// @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue, RejectedExecutionHandler)
    public NaeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /// @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue, ThreadFactory, RejectedExecutionHandler)
    public NaeThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /// {@inheritDoc}
    @Override
    public <T> @NotNull Future<T> submit(@NotNull Runnable task, T result) {
        return super.submit(new DelegatingSecurityContextRunnable(task), result);
    }

    /// {@inheritDoc}
    @Override
    public @NotNull Future<?> submit(@NotNull Runnable task) {
        return super.submit(new DelegatingSecurityContextRunnable(task));
    }

    /// {@inheritDoc}
    @Override
    public <T> @NotNull Future<T> submit(@NotNull Callable<T> task) {
        return super.submit(new DelegatingSecurityContextCallable<>(task));
    }

    /// {@inheritDoc}
    @Override
    public void execute(@NotNull Runnable command) {
        super.execute(new DelegatingSecurityContextRunnable(command));
    }

}
