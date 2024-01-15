package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticJob;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.domain.ElasticTaskJob;
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Service
public class ElasticTaskQueueManager {

    private final ThreadPoolTaskExecutor elasticTaskExecutor;

    private final ElasticTaskRepository repository;

    private final ConcurrentHashMap<String, BlockingQueue<Runnable>> taskQueues = new ConcurrentHashMap<>();
    private final Set<String> activeTasks = ConcurrentHashMap.newKeySet();

    public ElasticTaskQueueManager(@Qualifier("elasticTaskExecutor") ThreadPoolTaskExecutor elasticTaskExecutor, ElasticTaskRepository repository) {
        this.elasticTaskExecutor = elasticTaskExecutor;
        this.repository = repository;
    }


    public Future<ElasticTask> scheduleOperation(ElasticTaskJob task) {
        if (task.getTask().getTaskId() == null) {
            throw new IllegalArgumentException("Task id cannot be null");
        }

        String taskId = task.getTask().getTaskId();
        log.debug("Scheduling operation for task: {}", taskId);

        CompletableFuture<ElasticTask> future = new CompletableFuture<>();
        Runnable taskWrapper = () -> {
            try {
                ElasticTask result = processTask(task);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        BlockingQueue<Runnable> queue = taskQueues.computeIfAbsent(taskId, k -> new LinkedBlockingQueue<>());
        queue.add(taskWrapper);

        if (activeTasks.add(taskId)) {
            log.debug("Task {} is ready for processing. Submitting to executor.", taskId);
            elasticTaskExecutor.submit(queue.poll());
        } else {
            log.debug("Task {} is queued for processing.", taskId);
        }
        return future;
    }

    private ElasticTask processTask(ElasticTaskJob task) {
        try {
            log.debug("Processing task: {}", task.getTask().getTaskId());
            switch (task.getTypeJob()) {
                case INDEX:
                    return indexTaskWorker(task.getTask());
                case REMOVE:
                    return removeTaskWorker(task.getTask());
                default:
                    log.warn("Unknown task type for task: {}", task.getTask().getTaskId());
                    return null;
            }
        } catch (Exception e) {
            log.error("Error processing task {}: {}", task.getTask().getTaskId(), e.getMessage(), e);
            throw e;
        } finally {
            activeTasks.remove(task.getTask().getTaskId());
            scheduleNextTask(task.getTask().getTaskId());
        }
    }


    private void scheduleNextTask(String taskId) {
        BlockingQueue<Runnable> queue = taskQueues.get(taskId);
        if (queue != null) {
            Runnable nextTask = queue.poll();
            if (nextTask != null) {
                log.debug("Submitting next task for ID: {} to executor", taskId);
                elasticTaskExecutor.submit(nextTask);
            } else {
                activeTasks.remove(taskId);
                taskQueues.remove(taskId);
            }
        }
    }


    @PreDestroy
    public void destroy() throws InterruptedException {
        log.info("Shutting down ElasticTaskQueueManager");
        elasticTaskExecutor.shutdown();
    }


    private ElasticTask indexTaskWorker(ElasticTask task) {
        log.debug("Indexing task [{}] in thread [{}]", task.getTaskId(), Thread.currentThread().getName());
        ElasticTask elasticTask = null;
        try {
            elasticTask = repository.findByStringId(task.getStringId());
            if (elasticTask == null) {
                elasticTask = repository.save(task);
            } else {
                elasticTask.update(task);
                elasticTask = repository.save(elasticTask);
            }
            log.debug("[{}]: Task \"{}\" [{}] indexed", task.getCaseId(), task.getTitle(), task.getStringId());
        } catch (InvalidDataAccessApiUsageException e) {
            log.debug("[{}]: Task \"{}\" has duplicates, will be reindexed", task.getCaseId(), task.getTitle());
            repository.deleteAllByStringId(task.getStringId());
            repository.save(task);
            log.debug("[{}]: Task \"{}\" indexed", task.getCaseId(), task.getTitle());
        } catch (RuntimeException e) {
            log.error("Elastic executor was killed before finish: {}", e.getMessage());
        }
        return elasticTask;
    }

    private ElasticTask removeTaskWorker(ElasticTask task) {
        log.debug("Remove task [{}] in thread [{}]", task.getTaskId(), Thread.currentThread().getName());
        try {
            log.debug("[{}]: Task \"{}\" [{}] removed", task.getCaseId(), task.getTitle(), task.getStringId());
            return repository.deleteAllByTaskId(task.getTaskId());
        } catch (RuntimeException e) {
            log.error("Elastic executor was killed before finish: {}", e.getMessage());
        }
        return task;
    }

    public void removeTasksByProcess(String processId) {
        List<ElasticTask> tasks = repository.findAllByProcessId(processId);
        tasks.forEach(task -> {
            try {
                ElasticTaskJob job = new ElasticTaskJob(ElasticJob.REMOVE, task);
                Future<ElasticTask> totok = scheduleOperation(job);
                totok.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                log.error("[ExecutionException] Elastic executor was killed before finish: {}", e.getMessage());
                log.error(e.toString());
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                log.error("[InterruptedException] Elastic executor was killed before finish: {}", e.getMessage());
                log.error(e.toString());
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                log.error("[TimeoutException] Elastic executor was killed before finish: {}", e.getMessage());
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        });
    }

}
