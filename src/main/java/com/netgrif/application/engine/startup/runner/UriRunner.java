package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(80)
@RequiredArgsConstructor
public class UriRunner implements ApplicationEngineStartupRunner {

    private final IUriService uriService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        uriService.createDefault();
    }

}
