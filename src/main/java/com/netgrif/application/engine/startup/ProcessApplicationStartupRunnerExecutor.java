package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.startup.annotation.BeforeRunner;
import com.netgrif.application.engine.startup.runner.FinisherSuperCreatorRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@BeforeRunner(FinisherSuperCreatorRunner.class)
public class ProcessApplicationStartupRunnerExecutor extends AbstractOrderedApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<ProcessApplicationStartupRunner> runners = resolveRunners();
        log.info("Detected {} startup runners", runners.size());
        runners.forEach(runner -> {
            try {
                log.info("Runner {} starting", runner.getClass().getSimpleName());
                runner.run(args);
                log.info("Runner {} ended", runner.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Startup runner {} has failed", runner.getClass().getSimpleName(), e);
            }
        });
    }

    protected List<ProcessApplicationStartupRunner> resolveRunners() {
        Map<String, ProcessApplicationStartupRunner> customRunners = ApplicationContextProvider.getAppContext().getBeansOfType(ProcessApplicationStartupRunner.class);
        ApplicationRunnerOrderResolver.SortedRunners<ProcessApplicationStartupRunner> runners = ApplicationRunnerOrderResolver.sortByRunnerOrderAnnotation(customRunners.values());
        runners.sortUnresolvedRunners();
        return runners.getSorted();
    }

}
