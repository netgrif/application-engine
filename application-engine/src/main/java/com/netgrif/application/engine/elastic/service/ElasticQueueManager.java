package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.BulkOperationWrapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Manages a queue of elastic queries and handles their periodic bulk processing in Elasticsearch.
 * <p>
 * This class is responsible for:
 * - Enqueuing bulk operations and managing a bounded queue to ensure memory efficiency.
 * - Automatically flushing queued operations into Elasticsearch in configurable batches.
 * - Controlling execution with a scheduled timer to avoid overloading Elasticsearch.
 * - Handling exceptions during bulk operations; failed operations are retried via scheduled reindexing or manual endpoints.
 * <p>
 * Key components of this class:
 * - {@code queue}: A thread-safe queue to hold bulk operations before processing.
 * - {@code scheduler}: A scheduled executor service that manages timed execution of flush operations.
 * - {@code atomicDelayer}: An atomic reference ensuring only one active scheduled task for the flush timer.
 * - {@code queueProperties}: Configuration controlling maximum queue size, batch size, and flush delay parameters.
 * - {@code elasticsearchClient}: The Elasticsearch client used for direct communication with the cluster.
 * - {@code eventPublisher}: Publishes relevant events generated during bulk operation processing.
 **/
public final class ElasticQueueManager {

    private final Logger log = LoggerFactory.getLogger(ElasticQueueManager.class);

    private final BlockingQueue<BulkOperationWrapper> queue;

    private final ScheduledExecutorService scheduler;

    private final AtomicReference<ScheduledFuture<?>> atomicDelayer;

    private final DataConfigurationProperties.ElasticsearchProperties.QueueProperties queueProperties;

    private final ElasticsearchClient elasticsearchClient;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs an ElasticQueueManager instance.
     *
     * @param elasticsearchProperties the configuration properties for Elasticsearch, including queue parameters
     *                                such as maximum queue size, batch size, and flush delay.
     * @param elasticsearchClient     the Elasticsearch client used to perform bulk operations.
     * @param eventPublisher          an event publisher for broadcasting events related to processed operations.
     */
    public ElasticQueueManager(DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties,
                               ElasticsearchClient elasticsearchClient,
                               ApplicationEventPublisher eventPublisher) {
        atomicDelayer = new AtomicReference<>();
        queueProperties = elasticsearchProperties.getQueue();
        queue = new LinkedBlockingDeque<>(queueProperties.getMaxQueueSize());
        scheduler = Executors.newScheduledThreadPool(queueProperties.getScheduledExecutorPoolSize());
        this.elasticsearchClient = elasticsearchClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Shuts down the ElasticQueueManager gracefully.
     * <p>
     * This method cancels any pending scheduled tasks, flushes all remaining elements in the queue
     * to Elasticsearch, and shuts down the executor service. It ensures that no queued operations
     * are lost during the application shutdown process.
     **/
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
     * Adds a bulk operation to the queue and (re)starts the flush timer if needed.
     * <p>
     * If the queue size is below the configured batch size, the flush operation
     * is rescheduled to execute after the appropriate delay interval.
     *
     * @param operation the bulk operation to be added to the queue.
     */
    public void push(BulkOperationWrapper operation) {
        try {
            queue.put(operation);
            if (queue.size() < queueProperties.getMaxBatchSize()) {
                resetTimer();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while pushing to operation queue", e);
        }
    }

    /**
     * Processes and synchronously flushes a batch of elements from the queue to Elasticsearch.
     * <p>
     * Retrieves a batch of operations up to the configured maximum size from the queue
     * and sends them to Elasticsearch using the bulk API. If a failure occurs during the
     * bulk operation, the failed batch remains unprocessed and will be retried later via
     * scheduled execution or manual flush invocation. All successfully processed operations
     * trigger their respective publishable events.
     * <p>
     * This method is thread-safe and ensures that only one flush operation executes at any time.
     */
    private synchronized void flush() {
        if (queue.isEmpty()) {
            return;
        }

        List<BulkOperationWrapper> batch = new ArrayList<>();
        while (!queue.isEmpty() && batch.size() < queueProperties.getMaxBatchSize()) {
            batch.add(queue.poll());
        }

        String uuid = UUID.randomUUID().toString();
        try {
            log.debug("Index started with batch size: {} and id: {}", batch.size(), uuid);
            elasticsearchClient.bulk(new BulkRequest.Builder().operations(batch.stream().map(BulkOperationWrapper::getOperation).toList()).refresh(queueProperties.getRefreshPolicy()).build());
            log.debug("Index finished with batch size: {} and id: {}", batch.size(), uuid);
            checkQueue();
        } catch (Exception e) {
            log.error("Bulk operation failed for batch id: {} with {} operations. " +
                            "Operations will be retried via scheduled indexing or manual reindex.",
                    uuid, batch.size(), e);
            return;
        }
        try {
            publishEventsOfBatch(batch);
        } catch (Exception e) {
            log.error("Event publishing failed for batch id: {}", uuid, e);
        }
    }

    /**
     * Resets and reschedules the flush timer for delayed execution.
     * <p>
     * This method ensures that the flush operation is rescheduled to occur after
     * the configured delay interval. If a timer is already active, it is cancelled
     * before restarting a new one. This allows the system to adapt to new operations
     * being continuously added to the queue.
     */
    private synchronized void resetTimer() {
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

    /**
     * Checks the queue size and determines whether to flush or reschedule the timer.
     * <p>
     * If the queue size reaches or exceeds the configured maximum batch size, it
     * immediately triggers a flush operation. Otherwise, the timer is reset to wait
     * for additional operations.
     */
    private void checkQueue() {
        if (queue.size() >= queueProperties.getMaxBatchSize()) {
            flush();
        } else {
            resetTimer();
        }
    }

    /**
     * Publishes events for all operations in the batch that contain a publishable event.
     * <p>
     * This method iterates through the batch, checking each operation wrapper for an
     * associated event. If present, the event is published using the configured
     * {@code ApplicationEventPublisher}.
     *
     * @param batch the list of bulk operation wrappers to process for event publishing.
     */
    private void publishEventsOfBatch(List<BulkOperationWrapper> batch) {
        batch.stream().filter(operationWrapper -> operationWrapper.getPublishableEvent() != null)
                .forEach(operationWrapper -> eventPublisher.publishEvent(operationWrapper.getPublishableEvent()));
    }
}
