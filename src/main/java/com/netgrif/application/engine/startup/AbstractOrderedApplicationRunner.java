package com.netgrif.application.engine.startup;

import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

public abstract class AbstractOrderedApplicationRunner implements ApplicationRunner, Ordered {

    @Override
    public int getOrder() {
        return ApplicationRunnerOrderResolver.getOrder(this.getClass());
    }

}
