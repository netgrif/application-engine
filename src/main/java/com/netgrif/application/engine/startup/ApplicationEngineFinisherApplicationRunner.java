package com.netgrif.application.engine.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApplicationEngineFinisherApplicationRunner extends ApplicationRunnerExecutor<ApplicationEngineFinishRunner> {

    public ApplicationEngineFinisherApplicationRunner(ApplicationRunnerOrderResolver orderResolver, ApplicationRunnerProperties properties) {
        super(orderResolver, properties);
    }

    @Override
    public void executeRunner(ApplicationEngineFinishRunner runner, ApplicationArguments args) throws Exception {
        runner.run(args);
    }

}
