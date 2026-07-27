package com.netgrif.application.engine

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service

import java.util.concurrent.atomic.AtomicBoolean

@Service
class AsyncRunner {

    private final TaskExecutor actionsExecutor

    AsyncRunner(@Qualifier("actionsExecutor") TaskExecutor actionsExecutor) {
        this.actionsExecutor = actionsExecutor
    }

    void run(Closure closure) {
        ActionDelegate actionDelegate = findActionDelegate(closure)
        actionDelegate?.retainForAsyncExecution()
        AtomicBoolean released = new AtomicBoolean()

        Runnable task = {
            try {
                closure()
            } finally {
                release(actionDelegate, released)
            }
        } as Runnable

        try {
            execute(task)
        } catch (Throwable throwable) {
            release(actionDelegate, released)
            throw throwable
        }
    }

    void execute(final Runnable runnable) {
        actionsExecutor.execute(runnable)
    }

    private static void release(ActionDelegate actionDelegate, AtomicBoolean released) {
        if (actionDelegate != null && released.compareAndSet(false, true)) {
            actionDelegate.releaseAfterAsyncExecution()
        }
    }

    private static ActionDelegate findActionDelegate(Closure closure) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>())
        return findActionDelegate(closure, visited)
    }

    private static ActionDelegate findActionDelegate(Object candidate, Set<Object> visited) {
        if (candidate == null || !visited.add(candidate)) {
            return null
        }
        if (candidate instanceof ActionDelegate) {
            return candidate
        }
        if (!(candidate instanceof Closure)) {
            return null
        }

        Closure nestedClosure = (Closure) candidate
        return findActionDelegate(nestedClosure.delegate, visited)
                ?: findActionDelegate(nestedClosure.owner, visited)
                ?: findActionDelegate(nestedClosure.thisObject, visited)
    }
}
