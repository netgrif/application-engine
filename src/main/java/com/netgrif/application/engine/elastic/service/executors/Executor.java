package com.netgrif.application.engine.elastic.service.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class Executor {

    public static final Logger log = LoggerFactory.getLogger(Executor.class);

    private static final long EXECUTOR_TIMEOUT = 10;

    private Map<String, ExecutorService> executors;

    public Executor(@Value("${spring.data.elasticsearch.executors.size:500}") long maxSize, @Value("${spring.data.elasticsearch.executors.timeout:5}") long timeout) {
        this.executors = Collections.synchronizedMap(new ExecutorMaxSizeHashMap(maxSize, timeout));
        log.info("Executor created, thread capacity: " + maxSize);
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        this.executors.forEach((id, executor) -> {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS)) {
                        log.error("Executor " + id + " did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                log.error("Thread (executor " + id + ") was interrupted while waiting for termination: ", e);
                executor.shutdownNow();
            }
        });
    }

    public void execute(String id, Runnable task) {
        try {
            ExecutorService executorService = executors.get(id);

            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
                ExecutorService absent = executors.putIfAbsent(id, executorService);

                if (absent != null) {
                    absent.execute(task);
                    return;
                }
            }
            executorService.execute(task);
        } catch (RuntimeException e) {
            log.error("Elastic executor was killed before finish: " + e.getMessage());
        }
    }
}
