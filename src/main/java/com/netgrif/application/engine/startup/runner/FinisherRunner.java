package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineFinishRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(220)
public class FinisherRunner implements ApplicationEngineFinishRunner {

    @Override
    public void run(ApplicationArguments strings) throws Exception {
        log.info("+----------------------------+");
        log.info("| Netgrif Application Engine |");
        log.info("+----------------------------+");
    }

}
