package com.netgrif.application.engine.elastic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Profile("dev")
@Component
class ElasticsearchExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String[] ignored = {
            "Request execution cancelled",
            "Connection closed unexpectedly"
    };

    ElasticsearchExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e.getCause() != null && Arrays.stream(ignored).anyMatch(m -> m.equals(e.getCause().getMessage()))) {
            return;
        }
        log.error(e.getMessage(), e);
    }
}