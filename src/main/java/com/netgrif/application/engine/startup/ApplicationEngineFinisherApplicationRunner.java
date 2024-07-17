package com.netgrif.application.engine.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class ApplicationEngineFinisherApplicationRunner extends ApplicationRunnerExecutor<ApplicationEngineFinishRunner> {

    @Override
    public void executeRunner(ApplicationEngineFinishRunner runner, ApplicationArguments args) throws Exception {
        runner.run(args);
    }

}
