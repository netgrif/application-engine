package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Manages a queue of elastic queries and handles their periodic bulk processing in Elasticsearch.
 * This class efficiently buffers and schedules elastic queries in batches, ensuring timely
 * indexing into the Elasticsearch index with controlled execution and concurrency.
 *
 * @param <E> the type of elements to be managed in the queue and indexed by Elasticsearch
 */
public final class ElasticQueueManager<E> {

    private final Logger log = LoggerFactory.getLogger(ElasticQueueManager.class);

    private final Queue<BulkOperation> queue;

    private final ScheduledExecutorService scheduler;

    private final AtomicReference<ScheduledFuture<?>> atomicDelayer;

    private final DataConfigurationProperties.ElasticsearchProperties.QueueProperties queueProperties;


    private final ElasticsearchClient elasticsearchClient;

    /**
     * Constructs an ElasticQueueManager instance.
     *
     * @param elasticsearchProperties the configuration properties for Elasticsearch, including queue parameters
     */
    public ElasticQueueManager(DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties,
                               ElasticsearchClient elasticsearchClient) {
        queue = new ConcurrentLinkedDeque<>();
        atomicDelayer = new AtomicReference<>();
        queueProperties = elasticsearchProperties.getQueue();
        scheduler = Executors.newScheduledThreadPool(queueProperties.getScheduledExecutorPoolSize());
        this.elasticsearchClient = elasticsearchClient;
    }

    /**
     * Shuts down the scheduler and flushes any remaining elements in the queue
     * to Elasticsearch before the application stops.
     */
    @PreDestroy
    public void shutdown() {
        ScheduledFuture<?> delayer = atomicDelayer.getAndSet(null);
        if (delayer != null) {
            delayer.cancel(false);
        }
        scheduler.shutdown();
        while (!queue.isEmpty()) {
            flush();
        }
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }


    /**
     * Adds an elastic query to the queue and resets the timer for the scheduled flush.
     *
     * @param elasticQuery the elastic query to be added to the queue
     */
    public void push(BulkOperation elasticQuery) {
        queue.add(elasticQuery);
        if (queue.size() <= queueProperties.getMaxQueueSize()) {
            resetTimer();
        }
    }

    /**
     * Synchronously processes and flushes a batch of elements from the queue
     * to Elasticsearch. Ensures that no more than the maximum queue size is
     * processed at a time.
     */
    private synchronized void flush() {
        if (queue.isEmpty()) {
            return;
        }

        List<BulkOperation> batch = new ArrayList<>();
        while (!queue.isEmpty() && batch.size() < queueProperties.getMaxQueueSize()) {
            batch.add(queue.poll());
        }

        String uuid = UUID.randomUUID().toString();
        try {
            log.debug("Index started with batch size: {} and id: {}", batch.size(), uuid);
            elasticsearchClient.bulk(new BulkRequest.Builder().operations(batch).refresh(Refresh.False).build());
            log.debug("Index finished with batch size: {} and id: {}", batch.size(), uuid);
            if (queue.size() >= queueProperties.getMaxQueueSize()) {
                flush();
            } else {
                resetTimer();
            }
        } catch (Exception e) {
            queue.addAll(batch);
            log.error("Index failed with batch size: {} and id: {}", batch.size(), uuid, e);
        }
    }

    /**
     * Resets the timer for executing the flush operation. This ensures
     * that the flush operation is delayed until the configured time interval
     * has elapsed from the last push to the queue.
     */
    private void resetTimer() {
        if (scheduler.isShutdown()) {
            return;
        }
        ScheduledFuture<?> delayer = atomicDelayer.getAndSet(null);
        if (delayer != null) {
            delayer.cancel(false);
        }
        ScheduledFuture<?> newTask = scheduler.schedule(this::flush, queueProperties.getDelay(), queueProperties.getDelayUnit());
        atomicDelayer.set(newTask);
    }

}
