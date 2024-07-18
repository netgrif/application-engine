package com.netgrif.application.engine.startup;

import com.netgrif.application.engine.startup.annotation.AfterRunner;
import com.netgrif.application.engine.startup.annotation.BeforeRunner;
import com.netgrif.application.engine.startup.annotation.ReplaceRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationRunnerOrderResolver {

    private final ApplicationRunnerProperties properties;

    /**
     * Sorts the given collection of runners by the {@link RunnerOrder} annotation.
     *
     * @param <T>     the type of the runners
     * @param runners the collection of runners to be sorted
     * @return a {@link SortedRunners} object containing two lists: one with the sorted runners and one with the unresolved runners.
     * To resolve order of the unresolved list call {@link SortedRunners#sortUnresolvedRunners()} method.
     */
    public <T> SortedRunners<T> sortByRunnerOrderAnnotation(Collection<T> runners) {
        if (runners == null) return null;
        if (runners.isEmpty()) return new SortedRunners<>();
        List<T> unresolved = new ArrayList<>();
        TreeMap<Integer, List<T>> ordered = new TreeMap<>();
        runners.forEach(runner -> {
            Class<?> runnerClass = resolveClass(runner);
            RunnerOrder[] runnerOrders = runnerClass.getAnnotationsByType(RunnerOrder.class);
            if (runnerOrders == null || runnerOrders.length == 0) {
                unresolved.add(runner);
                return;
            }
            int numberOfExecutions = properties.isEnableMultipleExecution() ? runnerOrders.length : 1;
            for (int i = 0; i < numberOfExecutions; i++) {
                RunnerOrder order = runnerOrders[i];
                if (!ordered.containsKey(order.value())) {
                    ordered.put(order.value(), new ArrayList<>());
                }
                ordered.get(order.value()).add(runner);
            }
        });
        return new SortedRunners<>(ordered.values().stream().flatMap(List::stream).toList(), unresolved);
    }

    public static <T> Class<?> resolveClass(T object) {
        if (object instanceof Class) return (Class<?>) object;
        else return AopUtils.isAopProxy(object) ? AopUtils.getTargetClass(object) : object.getClass();
    }

    @Getter
    public static class SortedRunners<T> {
        private final List<T> sorted;
        private final List<T> unresolved;
        private final Map<Class<?>, Class<?>> replaced = new HashMap<>(); // key is active runner, value is runner that was replaced

        public SortedRunners() {
            sorted = new ArrayList<>();
            unresolved = new ArrayList<>();
        }

        public SortedRunners(List<T> sorted) {
            this.sorted = new ArrayList<>(sorted);
            unresolved = new ArrayList<>();
        }

        public SortedRunners(List<T> sorted, List<T> unresolved) {
            this.sorted = new ArrayList<>(sorted);
            this.unresolved = new ArrayList<>(unresolved);
        }

        /**
         * Attempts to sort the unresolved runners by processing annotations on their classes.
         * The method iterates through the list of unresolved runners and checks for the presence
         * of specific annotations: {@link BeforeRunner}, {@link AfterRunner}, and {@link ReplaceRunner}.
         * Depending on the annotation found, the runner is inserted into the appropriate position
         * in the sorted list. If a runner is successfully inserted, it is removed from the unresolved list.
         * The method recursively calls itself if changes are made to ensure all runners are processed.
         *
         * @return {@code true} if all unresolved runners have been successfully sorted and the unresolved list is empty;
         * {@code false} otherwise.
         */
        public boolean sortUnresolvedRunners() {
            boolean changed = false;
            changed = changed || resolveSortingAnnotation(BeforeRunner.class, this::insertBeforeRunner);
            changed = changed || resolveSortingAnnotation(AfterRunner.class, this::insertAfterRunner);
            changed = changed || resolveSortingAnnotation(ReplaceRunner.class, this::replaceRunner);
            if (unresolved.isEmpty()) return true;
            if (changed) changed = sortUnresolvedRunners();
            return changed;
        }

        protected boolean resolveSortingAnnotation(Class<? extends Annotation> annotation, Function<T, Boolean> resolver) {
            boolean changed = false;
            for (int i = unresolved.size() - 1; i >= 0; i--) {
                T runner = unresolved.get(i);
                Class<?> runnerClass = resolveClass(runner);
                boolean inserted = false;
                if (runnerClass.isAnnotationPresent(annotation)) {
                    inserted = resolver.apply(runner);
                }
                if (!inserted) continue;
                unresolved.remove(i);
                changed = true;
            }
            return changed;
        }

        protected boolean insertBeforeRunner(T item) {
            Class<?> itemClass = resolveClass(item);
            if (!itemClass.isAnnotationPresent(BeforeRunner.class)) return false;
            Class<?> orderedRunner = itemClass.getAnnotation(BeforeRunner.class).value();
            int orderedRunnerIndex = indexOfClass(sorted, orderedRunner);
            if (orderedRunnerIndex == -1) return false;
            sorted.add(orderedRunnerIndex, item);
            return true;
        }

        protected boolean insertAfterRunner(T item) {
            Class<?> itemClass = resolveClass(item);
            if (!itemClass.isAnnotationPresent(AfterRunner.class)) return false;
            Class<?> orderedRunner = itemClass.getAnnotation(AfterRunner.class).value();
            int orderedRunnerIndex = indexOfClass(sorted, orderedRunner);
            if (orderedRunnerIndex == -1) return false;
            if (orderedRunnerIndex + 1 == sorted.size()) {
                sorted.add(item);
            } else {
                sorted.add(orderedRunnerIndex + 1, item);
            }
            return true;
        }

        protected boolean replaceRunner(T item) {
            Class<?> itemClass = resolveClass(item);
            if (!itemClass.isAnnotationPresent(ReplaceRunner.class)) return false;
            Class<?>[] runnersToReplace = itemClass.getAnnotation(ReplaceRunner.class).value();
            boolean changed = false;
            for (Class<?> runnerToReplace : runnersToReplace) {
                int runnerToReplaceIndex = indexOfClass(sorted, runnerToReplace);
                if (runnerToReplaceIndex == -1) continue;
                sorted.add(runnerToReplaceIndex, item);
                replaced.put(itemClass, runnerToReplace);
                changed = true;
            }
            return changed;
        }

        /**
         * Returns the index of the first occurrence of the specified class in the given list.
         * If the list contains an element whose class matches the specified class, the index of that element is returned.
         * If the specified class is {@code null}, the method returns the index of the first {@code null} element in the list.
         * If the list is {@code null} or empty, or if the class is not found, the method returns {@code -1}.
         *
         * @param <I>   the type of elements in the list
         * @param list  the list to search for the specified class
         * @param clazz the class to search for in the list
         * @return the index of the first occurrence of the specified class in the list, or {@code -1} if the class is not found
         */
        public static <I> int indexOfClass(List<I> list, Class<?> clazz) {
            if (list == null) return -1;
            if (list.isEmpty()) return -1;
            if (clazz == null) return list.indexOf(null);
            for (int i = 0; i < list.size(); i++) {
                if (resolveClass(list.get(i)).equals(clazz)) {
                    return i;
                }
            }
            return -1;
        }

    }

}
