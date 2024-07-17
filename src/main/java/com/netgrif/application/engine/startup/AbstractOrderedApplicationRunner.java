package com.netgrif.application.engine.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

@Slf4j
public abstract class AbstractOrderedApplicationRunner implements ApplicationRunner, Ordered {

    @Override
    public int getOrder() {
        return ApplicationRunnerOrderResolver.getOrder(this.getClass());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (ApplicationRunnerOrderResolver.isReplaced(this.getClass())) {
            log.warn("Runner {} is skipped as it is replaced by runner {}", this.getClass().getSimpleName(), ApplicationRunnerOrderResolver.getReplacement(this.getClass()).getSimpleName());
        } else {
            apply(args);
        }
    }

    /**
     * Executing business logic of implemented runner
     *
     * @param args arguments passed to the application
     * @throws Exception
     */
    public abstract void apply(ApplicationArguments args) throws Exception;

}
