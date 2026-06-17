package com.netgrif.application.engine.startup;

import org.springframework.boot.ApplicationArguments;

@FunctionalInterface
public interface ApplicationEngineFinishRunner {

    void run(ApplicationArguments args) throws Exception;

}
