package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.startup.annotation.BeforeRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.application.engine.startup.runner.MongoDbRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
@BeforeRunner(MongoDbRunner.class)
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
        TreeMap<Integer, List<ProcessApplicationStartupRunner>> orderedRunners = new TreeMap<>();
        customRunners.forEach((k, v) -> {
            RunnerOrder orderAnnotation = v.getClass().getAnnotation(RunnerOrder.class);
            int order = orderAnnotation == null ? Integer.MAX_VALUE : orderAnnotation.value();
            if (!orderedRunners.containsKey(order)) {
                orderedRunners.put(order, new ArrayList<>());
            }
            orderedRunners.get(order).add(v);
        });
        return orderedRunners.values().stream().flatMap(List::stream).toList();
    }

}
