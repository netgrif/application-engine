package com.netgrif.application.engine.startup;

import org.springframework.boot.ApplicationArguments;

@FunctionalInterface
public interface ApplicationEngineStartupRunner {

    void run(ApplicationArguments args) throws Exception;

}
