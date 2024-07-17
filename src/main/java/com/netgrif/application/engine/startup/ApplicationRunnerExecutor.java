package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.startup.annotation.OptionalRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.GenericTypeResolver;

import java.util.Map;

import static com.netgrif.application.engine.startup.ApplicationRunnerOrderResolver.resolveClass;

@Slf4j
public abstract class ApplicationRunnerExecutor<T> implements ApplicationRunner {

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
        });
    }

    protected ApplicationRunnerOrderResolver.SortedRunners<T> resolveRunners() {
        Map<String, T> customRunners = (Map<String, T>) ApplicationContextProvider.getAppContext().getBeansOfType(GenericTypeResolver.resolveTypeArgument(getClass(), ApplicationRunnerExecutor.class));
        ApplicationRunnerOrderResolver.SortedRunners<T> runners = ApplicationRunnerOrderResolver.sortByRunnerOrderAnnotation(customRunners.values());
        runners.sortUnresolvedRunners();
        return runners;
    }

}
