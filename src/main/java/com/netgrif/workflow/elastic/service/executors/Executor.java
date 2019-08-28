package com.netgrif.workflow.elastic.service.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class Executor {

    public static final Logger log = LoggerFactory.getLogger(Executor.class);

    private static final long EXECUTOR_TIMEOUT = 10;

    private ThreadPoolExecutor executor;
    private Map<String, ExecutorService> executors;

    public Executor(@Value("${spring.data.elasticsearch.executors:500}") long maxSize, @Value("${spring.data.elasticsearch.executors.timeout:5}") long timeout) {
        this.executors = Collections.synchronizedMap(new MaxSizeHashMap(maxSize, timeout));
        log.info("[THREAD-EXECUTOR] Executor created, thread capacity: " + maxSize);

//        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool((int) maxSize);
//        this.executor.setCorePoolSize((int) maxSize);
//        this.executor.setKeepAliveTime(timeout, TimeUnit.SECONDS);
//        this.executor.allowCoreThreadTimeOut(true);
//        log.info("[THREAD-EXECUTOR] ThreadPoolExecutor created, thread capacity: " + executor.getCorePoolSize());
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        this.executors.forEach((id, executor) -> {
            try {
                executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
                executor.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("Thread was interrupted while waiting for termination");
            }
        });
//        terminateExecutor();
    }

    public void execute(String id, Runnable task) {
        ExecutorService executorService = executors.get(id);

        if (executorService == null) {
//            log.info("[ELASTIC-THREADS] All threads: " + Thread.activeCount());
            executorService = Executors.newSingleThreadExecutor();
//            log.info("[ELASTIC-THREADS] Creating new executor[" + executorService.toString() + "] for " + id);
            ExecutorService absent = executors.putIfAbsent(id, executorService);
//            log.info("[ELASTIC-THREADS] Size of executor cache: " + executors.size() + " / " + Thread.activeCount());

            if (absent != null) {
                absent.execute(task);
                return;
            }
        }
        executorService.execute(task);


//        log.info("[THREAD-EXECUTOR] Submitting task " + id + " to executor. Active threads: " + this.executor.getActiveCount());
//        this.executor.execute(task);
//        log.info("[THREAD-EXECUTOR] Task " + id + " submitted. Active threads: " + this.executor.getActiveCount());
    }

    public <T> Future<T> execute(String id, Callable<T> task) {
        log.info("[THREAD-EXECUTOR] Submitting task " + id + " to executor. Active threads: " + this.executor.getActiveCount());
        Future<T> future = this.executor.submit(task);
        log.info("[THREAD-EXECUTOR] Task " + id + " submitted. Active threads: " + this.executor.getActiveCount());
        return future;
    }

    private void terminateExecutor() throws InterruptedException {
        log.info("[THREAD-EXECUTOR] Terminating ThreadPoolExecutor with " + this.executor.getActiveCount() + " active threads");
        this.executor.shutdown();
        boolean terminated = this.executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
        if (!terminated) {
            log.warn("Executor does not terminate in specified time " + EXECUTOR_TIMEOUT + "s!");
            List<Runnable> dropped = this.executor.shutdownNow();
            log.warn("Executor was brutally shutdown. " + dropped.size() + " will not be executed!");
        }
    }

}