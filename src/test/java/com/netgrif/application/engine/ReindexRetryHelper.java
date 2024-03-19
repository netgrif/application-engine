package com.netgrif.application.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
@Profile("test")
public class ReindexRetryHelper<T> {

    private static final long DEFAULT_MAX_ATTEMPTS = 10;
    private static final long DEFAULT_INITIAL_WAIT_MS = 5000;
    private static final long DEFAULT_MAX_LIMIT_WAIT_MS = 120000;

    public ReindexRetryHelper() {
    }

    public static <T> T execute(Supplier<T> op, Predicate<T> test) throws InterruptedException {
        return execute(op, test, DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_WAIT_MS, DEFAULT_MAX_LIMIT_WAIT_MS, 2);
    }

    public static <T> T execute(Supplier<T> op, Predicate<T> test, Long waitTime, Long attempts) throws InterruptedException {
        return execute(op, test, attempts, waitTime, DEFAULT_MAX_LIMIT_WAIT_MS, 2);
    }

    // exponentialWait(op,max,timeout,).until

    public static <T> T execute(Supplier<T> operation, Predicate<T> resultTest, Long attempts, Long waitTimeMs, Long maxWaitTime, Integer waitTimeExponent) throws InterruptedException {
        log.debug("Starting operation with max attempts: {}", attempts);
        int attempt = 0;
        long waitTime = waitTimeMs;
        while (attempt < attempts) {
            T result = operation.get();
            if (resultTest.test(result)) {
                log.debug("Operation successful on attempt number: {}", attempt + 1);
                return result;
            }
            if (attempt < attempts - 1) {
                log.debug("Operation failed on attempt number {}. Retrying in {} ms", attempt + 1, waitTime);
                Thread.sleep(waitTime);
                waitTime *= waitTimeExponent;
                if (waitTime > maxWaitTime) {
                    waitTime = maxWaitTime;
                }
            }
            attempt++;
        }
        log.error("Failed to get expected result after {} attempts.", attempts);
        throw new AssertionError(String.format("Failed to get expected result after %d attempts.", attempts));
    }
}
