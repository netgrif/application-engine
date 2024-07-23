package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.startup.annotation.OptionalRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.netgrif.application.engine.startup.ApplicationRunnerOrderResolver.resolveClass;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class ApplicationRunnerExecutor<T> implements ApplicationRunner {

    protected final ApplicationRunnerOrderResolver orderResolver;
    protected final ApplicationRunnerProperties properties;

    public abstract void executeRunner(T runner, ApplicationArguments args) throws Exception;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ApplicationRunnerOrderResolver.SortedRunners<T> runners = resolveRunners();
        log.info("Executor {} has detected {} runners", this.getClass().getSimpleName(), runners.getSorted().size());
        if (!runners.getUnresolved().isEmpty()) {
            log.warn("{} runners order stayed unresolved: {}", runners.getUnresolved().size(), runners.getUnresolved());
        }
        runners.getSorted().forEach(runner -> {
            try {
                callRunner(runner, runners, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (properties.isRunUnresolved()) {
            log.info("Executing runners for which order couldn't be resolved");
            runners.getUnresolved().forEach(runner -> {
                try {
                    callRunner(runner, runners, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    protected void callRunner(T runner, ApplicationRunnerOrderResolver.SortedRunners<T> runners, ApplicationArguments args) throws Exception {
        try {
            Class<?> runnerClass = resolveClass(runner);
            if (runners.getReplaced().containsKey(runnerClass)) {
                log.warn("Runner {} is starting that replaced runner {}", runnerClass.getSimpleName(), runners.getReplaced().get(runnerClass).getSimpleName());
            } else {
                log.info("Runner {} is starting", runnerClass.getSimpleName());
            }
            executeRunner(runner, args);
            log.info("Runner {} has ended", runnerClass.getSimpleName());
        } catch (Exception e) {
            Class<?> runnerClass = resolveClass(runner);
            if (runnerClass.isAnnotationPresent(OptionalRunner.class)) {
                log.error("Runner {} has failed. Runner is flagged as optional, execution will continue", runnerClass.getSimpleName(), e);
            } else {
                log.error("Runner {} has failed", runnerClass.getSimpleName(), e);
                throw new RuntimeException(e);
            }
        }
    }

    protected ApplicationRunnerOrderResolver.SortedRunners<T> resolveRunners() {
        Map<String, T> customRunners = (Map<String, T>) ApplicationContextProvider.getAppContext().getBeansOfType(GenericTypeResolver.resolveTypeArgument(getClass(), ApplicationRunnerExecutor.class));
        ApplicationRunnerOrderResolver.SortedRunners<T> runners = orderResolver.sortByRunnerOrderAnnotation(customRunners.values());
        runners.resolveAllRunners();
        return runners;
    }

}
