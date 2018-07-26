package com.netgrif.workflow

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsyncRunner {

    @Async
    void run(Closure closure) {
        closure()
    }
}