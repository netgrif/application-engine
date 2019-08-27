package com.netgrif.workflow.elastic.service.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Executor {

    public static final Logger log = LoggerFactory.getLogger(Executor.class);

    private Map<String, ExecutorService> map;

    public Executor(@Value("${spring.data.elasticsearch.executors:500}") long maxSize, @Value("${spring.data.elasticsearch.executors.timeout:5}") long timeout) {
        this.map = Collections.synchronizedMap(new MaxSizeHashMap(maxSize, timeout));
    }

    public void execute(String id, Runnable task) {
        ExecutorService executorService = map.get(id);

        if (executorService == null) {
            log.info("[ELASTIC-THREADS] All threads: " + Thread.activeCount());
            executorService = Executors.newSingleThreadExecutor();
            log.info("[ELASTIC-THREADS] Creating new executor[" + executorService.toString() + "] for " + id);
            ExecutorService absent = map.putIfAbsent(id, executorService);
            log.info("[ELASTIC-THREADS] Size of executor cache: " + map.size() + " / " + Thread.activeCount());

            if (absent != null) {
                absent.execute(task);
                return;
            }
        }

        executorService.execute(task);
    }
}