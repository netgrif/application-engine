package com.netgrif.application.engine.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationShutdownProvider {

    private final ApplicationContext applicationContext;

    public void shutdown(Class<?> calledBy, int exitCode) {
        String className = calledBy == null ? "unknown" : calledBy.getSimpleName();
        log.info("Application was signalled by {} to shutdown; exit code: {}", className, exitCode);
        int ec = SpringApplication.exit(applicationContext, () -> exitCode);
        System.exit(ec);
    }

    public void shutdown(Class<?> calledBy) {
        shutdown(calledBy, 0);
    }

    public void shutdown() {
        shutdown(null, 0);
    }

}
