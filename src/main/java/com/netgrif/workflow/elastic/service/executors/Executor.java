package com.netgrif.workflow.elastic.service.executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Executor {

    private Map<String, ExecutorService> map;

    public Executor(@Value("${spring.data.elasticsearch.executors:10000}") long maxSize) {
        this.map = Collections.synchronizedMap(new MaxSizeHashMap(maxSize));
    }

    public void execute(String id, Runnable task) {
        ExecutorService executorService = map.get(id);

        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
            ExecutorService absent = map.putIfAbsent(id, executorService);

            if (absent != null) {
                absent.execute(task);
                return;
            }
        }

        executorService.execute(task);
    }
}