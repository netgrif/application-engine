package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.startup.annotation.AfterRunner;
import com.netgrif.application.engine.startup.annotation.BeforeRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class ApplicationRunnerOrderResolver {

    private static final List<Class<? extends AbstractOrderedApplicationRunner>> order = new ArrayList<>();

    /**
     * Retrieves the order index of the specified class within the registered application runners.
     *
     * @param clazz the class to find the order index for, which must extend {@code AbstractOrderedApplicationRunner}
     * @return the order index of the specified class
     * @throws IllegalArgumentException if the specified class is not registered as an application runner
     */
    public static int getOrder(Class<? extends AbstractOrderedApplicationRunner> clazz) {
        int idx = order.indexOf(clazz);
        if (idx == -1) {
            throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " is not registered as an application runner");
        }
        return idx;
    }

    @EventListener
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        List<Class<? extends AbstractOrderedApplicationRunner>> unresolved = new ArrayList<>();
        Map<String, AbstractOrderedApplicationRunner> applicationRunners = event.getApplicationContext().getBeansOfType(AbstractOrderedApplicationRunner.class);
        TreeMap<Integer, List<Class<? extends AbstractOrderedApplicationRunner>>> orderedRunners = new TreeMap<>();
        applicationRunners.forEach((k, v) -> {
            RunnerOrder order = v.getClass().getAnnotation(RunnerOrder.class);
            if (order == null) {
                unresolved.add(v.getClass());
                return;
            }
            if (!orderedRunners.containsKey(order.value())) {
                orderedRunners.put(order.value(), new ArrayList<>());
            }
            orderedRunners.get(order.value()).add(v.getClass());
        });
        order.clear();
        order.addAll(orderedRunners.values().stream().flatMap(List::stream).toList());
        sortUnresolvedRunner(unresolved);
        if (!unresolved.isEmpty()) {
            log.warn("Not all application runner were registered, unresolved application runners: {}", unresolved);
        }
    }

    protected boolean sortUnresolvedRunner(List<Class<? extends AbstractOrderedApplicationRunner>> unresolved) {
        boolean changed = false;
        for (int i = unresolved.size() - 1; i >= 0; i--) {
            Class<? extends AbstractOrderedApplicationRunner> runner = unresolved.get(i);
            boolean inserted = false;
            if (runner.isAnnotationPresent(BeforeRunner.class)) {
                inserted = insertBeforeRunner(runner);
            } else if (runner.isAnnotationPresent(AfterRunner.class)) {
                inserted = insertAfterRunner(runner);
            }
            if (!inserted) continue;
            unresolved.remove(i);
            changed = true;
        }
        if (unresolved.isEmpty()) return true;
        if (changed) changed = sortUnresolvedRunner(unresolved);
        return changed;
    }

    protected boolean insertBeforeRunner(Class<? extends AbstractOrderedApplicationRunner> runner) {
        if (!runner.isAnnotationPresent(BeforeRunner.class)) return false;
        Class<? extends AbstractOrderedApplicationRunner> orderedRunner = runner.getAnnotation(BeforeRunner.class).value();
        int orderedRunnerIndex = order.indexOf(orderedRunner);
        if (orderedRunnerIndex == -1) return false;
        order.add(orderedRunnerIndex, runner);
        return true;
    }

    protected boolean insertAfterRunner(Class<? extends AbstractOrderedApplicationRunner> runner) {
        if (!runner.isAnnotationPresent(AfterRunner.class)) return false;
        Class<? extends AbstractOrderedApplicationRunner> orderedRunner = runner.getAnnotation(AfterRunner.class).value();
        int orderedRunnerIndex = order.indexOf(orderedRunner);
        if (orderedRunnerIndex == -1) return false;
        if (orderedRunnerIndex + 1 == order.size()) {
            order.add(runner);
        } else {
            order.add(orderedRunnerIndex + 1, runner);
        }
        return true;
    }

}
