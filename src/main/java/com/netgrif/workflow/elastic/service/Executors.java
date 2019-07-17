package com.netgrif.workflow.elastic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class Executors {

    private static final Logger log = LoggerFactory.getLogger(Executors.class);

    @Value("${spring.data.elasticsearch.executors:10000}")
    private int limit;

    private LinkedHashMap<String, ExecutorService> executors = new LinkedHashMap<String, ExecutorService>(limit*10/7, 0.7f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ExecutorService> eldest) {
            return size() > limit;
        }
    };

    public void execute(String id, Runnable task) {
        if (!executors.containsKey(id)) {
            executors.put(id, java.util.concurrent.Executors.newSingleThreadExecutor());
        }
        ExecutorService executorService = executors.get(id);
        if (executorService != null) {
            executorService.execute(task);
        } else {
            execute(id, task);
        }
    }
}