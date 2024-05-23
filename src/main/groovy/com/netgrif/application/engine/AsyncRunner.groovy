package com.netgrif.application.engine

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsyncRunner {

    @SuppressWarnings('GrMethodMayBeStatic')
    @Async
    void run(Closure closure) {
        closure()
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @Async
    void execute(final Runnable runnable) {
        runnable.run()
    }
}