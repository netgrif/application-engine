package com.netgrif.application.engine.startup


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
abstract class AbstractOrderedCommandLineRunner implements CommandLineRunner, Ordered {

    @Autowired
    protected RunnerController runner

    @Override
    int getOrder() {
        return runner.getOrder(this.class)
    }
}