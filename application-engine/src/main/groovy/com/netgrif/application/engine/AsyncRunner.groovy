package com.netgrif.application.engine

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsyncRunner {

    @Async
    void run(Closure closure) {
        closure()
    }

    @Async
    void execute(final Runnable runnable) {
        runnable.run()
    }
}