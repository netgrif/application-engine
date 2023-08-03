package com.netgrif.application.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
@Profile("test")
public class ReindexRetryHelper<T> {

    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final long DEFAULT_INITIAL_WAIT_MS = 5000;
    private static final long DEFAULT_MAX_LIMIT_WAIT_MS = 120000;

    private final int maxAttempts;
    private final long waitTimeMs;

    public ReindexRetryHelper() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_WAIT_MS);
    }

    public ReindexRetryHelper(int maxAttempts, long initialWaitTimeMs) {
        this.maxAttempts = maxAttempts;
        this.waitTimeMs = initialWaitTimeMs;
    }

    public T execute(Supplier<T> operation, Predicate<T> resultTest) throws InterruptedException {
        log.debug("Starting operation with max attempts: {}", maxAttempts);
        int attempt = 0;
        long waitTime = waitTimeMs;
        while (attempt < maxAttempts) {
            T result = operation.get();

            if (resultTest.test(result)) {
                log.debug("Operation successful on attempt number: {}", attempt + 1);
                return result;
            }

            if (attempt < maxAttempts - 1) {
                log.debug("Operation failed on attempt number {}. Retrying in {} ms", attempt + 1, waitTime);
                Thread.sleep(waitTime);
                waitTime *= 2;
                if(waitTime > DEFAULT_MAX_LIMIT_WAIT_MS){
                    waitTime = DEFAULT_MAX_LIMIT_WAIT_MS;
                }
            }

            attempt++;
        }

        log.error("Failed to get expected result after {} attempts.", maxAttempts);
        throw new AssertionError(String.format("Failed to get expected result after %d attempts.", maxAttempts));
    }
}
