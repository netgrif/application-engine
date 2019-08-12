package com.netgrif.workflow.elastic.service.executors;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {

    private Map<String, ExecutorService> map;

    public Executor(long maxSize) {
        this.map = Collections.synchronizedMap(new MaxSizeHashMap(maxSize));
    }

    public void execute(String id, Runnable task) {
        ExecutorService executorService = map.get(id);
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
            map.put(id, executorService);
        }
        executorService.execute(task);
    }
}