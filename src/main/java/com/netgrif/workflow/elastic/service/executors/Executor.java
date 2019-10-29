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

    private Map<String, ExecutorService> executors;

    public Executor(@Value("${spring.data.elasticsearch.executors:500}") long maxSize, @Value("${spring.data.elasticsearch.executors.timeout:5}") long timeout) {
        this.executors = Collections.synchronizedMap(new MaxSizeHashMap(maxSize, timeout));
        log.info("Executor created, thread capacity: " + maxSize);
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        this.executors.forEach((id, executor) -> {
            try {
                executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
                executor.shutdown();
            } catch (InterruptedException e) {
                log.error("Thread was interrupted while waiting for termination: ", e);
            }
        });
    }

    public void execute(String id, Runnable task) {
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
    }
}