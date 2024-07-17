package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.startup.annotation.AfterRunner;
import com.netgrif.application.engine.startup.annotation.BeforeRunner;
import com.netgrif.application.engine.startup.annotation.ReplaceRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ApplicationRunnerOrderResolver {

    private static final List<Class<? extends AbstractOrderedApplicationRunner>> order = new ArrayList<>();

    /**
     * Retrieves the order index of the specified class within the registered application runners.
     *
     * @param clazz the class to find the order index for, which must extend {@link AbstractOrderedApplicationRunner}
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
        Map<String, AbstractOrderedApplicationRunner> applicationRunners = event.getApplicationContext().getBeansOfType(AbstractOrderedApplicationRunner.class);
        order.clear();
        SortedRunners<AbstractOrderedApplicationRunner> runners = sortByRunnerOrderAnnotation(applicationRunners.values());
        runners.sortUnresolvedRunners();
        runners.getSorted().forEach(r -> order.add((Class<? extends AbstractOrderedApplicationRunner>) resolveClass(r)));
        if (!runners.getUnresolved().isEmpty()) {
            log.warn("Not all application runner were registered, unresolved application runners: {}", runners.getUnresolved());
        }
    }

    /**
     * Sorts the given collection of runners by the {@link RunnerOrder} annotation.
     *
     * @param <T>     the type of the runners
     * @param runners the collection of runners to be sorted
     * @return a {@link SortedRunners} object containing two lists: one with the sorted runners and one with the unresolved runners.
     * To resolve order of the unresolved list call {@link SortedRunners#sortUnresolvedRunners()} method.
     */
    public static <T> SortedRunners<T> sortByRunnerOrderAnnotation(Collection<T> runners) {
        if (runners == null) return null;
        if (runners.isEmpty()) return new SortedRunners<>();
        List<T> unresolved = new ArrayList<>();
        TreeMap<Integer, List<T>> ordered = new TreeMap<>();
        runners.forEach(runner -> {
            Class<?> runnerClass = resolveClass(runner);
            RunnerOrder order = runnerClass.getAnnotation(RunnerOrder.class);
            if (order == null) {
                unresolved.add(runner);
                return;
            }
            if (!ordered.containsKey(order.value())) {
                ordered.put(order.value(), new ArrayList<>());
            }
            ordered.get(order.value()).add(runner);
        });
        return new SortedRunners<>(ordered.values().stream().flatMap(List::stream).toList(), unresolved);
    }

    protected static <T> Class<?> resolveClass(T object) {
        if (object instanceof Class) return (Class<?>) object;
        else return AopUtils.isAopProxy(object) ? AopUtils.getTargetClass(object) : object.getClass();
    }

    @Getter
    public static class SortedRunners<T> {
        private final List<T> sorted;
        private final List<T> unresolved;

        public SortedRunners() {
            sorted = new ArrayList<>();
            unresolved = new ArrayList<>();
        }

        public SortedRunners(List<T> sorted) {
            this.sorted = sorted;
            unresolved = new ArrayList<>();
        }

        public SortedRunners(List<T> sorted, List<T> unresolved) {
            this.sorted = sorted;
            this.unresolved = unresolved;
        }

        public SortedRunners<T> addSorted(T item) {
            sorted.add(item);
            return this;
        }

        public SortedRunners<T> addUnresolved(T item) {
            unresolved.add(item);
            return this;
        }

        public boolean sortUnresolvedRunners() {
            boolean changed = false;
            for (int i = unresolved.size() - 1; i >= 0; i--) {
                T runner = unresolved.get(i);
                Class<?> runnerClass = resolveClass(runner);
                boolean inserted = false;
                if (runnerClass.isAnnotationPresent(BeforeRunner.class)) {
                    inserted = insertBeforeRunner(runner);
                } else if (runnerClass.isAnnotationPresent(AfterRunner.class)) {
                    inserted = insertAfterRunner(runner);
                } else if(runnerClass.isAnnotationPresent(ReplaceRunner.class)){
                    inserted = replaceRunner(runner);
                }
                if (!inserted) continue;
                unresolved.remove(i);
                changed = true;
            }
            if (unresolved.isEmpty()) return true;
            if (changed) changed = sortUnresolvedRunners();
            return changed;
        }

        protected boolean insertBeforeRunner(T item) {
            Class<?> runner = resolveClass(item);
            if (!runner.isAnnotationPresent(BeforeRunner.class)) return false;
            Class<?> orderedRunner = runner.getAnnotation(BeforeRunner.class).value();
            int orderedRunnerIndex = sorted.indexOf(orderedRunner);
            if (orderedRunnerIndex == -1) return false;
            sorted.add(orderedRunnerIndex, item);
            return true;
        }

        protected boolean insertAfterRunner(T item) {
            Class<?> runner = resolveClass(item);
            if (!runner.isAnnotationPresent(AfterRunner.class)) return false;
            Class<?> orderedRunner = runner.getAnnotation(AfterRunner.class).value();
            int orderedRunnerIndex = sorted.indexOf(orderedRunner);
            if (orderedRunnerIndex == -1) return false;
            if (orderedRunnerIndex + 1 == sorted.size()) {
                sorted.add(item);
            } else {
                sorted.add(orderedRunnerIndex + 1, item);
            }
            return true;
        }

        protected boolean replaceRunner(T item){
            Class<?> runner = resolveClass(item);
            if(runner.isAnnotationPresent(ReplaceRunner.class)) return false;
            Class<?> runnerToReplace = runner.getAnnotation(ReplaceRunner.class).value();
            int runnerToReplaceIndex = sorted.indexOf(runnerToReplace);
            if(runnerToReplaceIndex == -1) return false;
            sorted.add(runnerToReplaceIndex, item);
            return true;
        }

    }

}
