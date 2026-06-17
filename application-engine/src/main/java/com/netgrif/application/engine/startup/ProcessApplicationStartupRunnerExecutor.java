package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.configuration.properties.RunnerConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(0)
@Component
public class ProcessApplicationStartupRunnerExecutor extends ApplicationRunnerExecutor<ProcessApplicationStartupRunner> {

    public ProcessApplicationStartupRunnerExecutor(ApplicationRunnerOrderResolver orderResolver, RunnerConfigurationProperties.ApplicationRunnerProperties properties) {
        super(orderResolver, properties);
    }

    @Override
    public void executeRunner(ProcessApplicationStartupRunner runner, ApplicationArguments args) throws Exception {
        runner.run(args);
    }

}
