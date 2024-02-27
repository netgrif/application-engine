package com.netgrif.application.engine.startup


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
abstract class AbstractOrderedCommandLineRunner implements CommandLineRunner, Ordered {

    protected RunnerController runner

    @Autowired
    void setRunner(RunnerController runner) {
        this.runner = runner
    }

    @Override
    int getOrder() {
        return runner.getOrder(this.class)
    }
}