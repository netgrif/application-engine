package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineFinishRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(210)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
public class FinisherSuperCreatorRunner implements ApplicationEngineFinishRunner {

    private final SuperCreatorRunner superCreator;

    @Override
    public void run(ApplicationArguments strings) throws Exception {
        superCreator.setAllToSuperUser();
        log.info("Super Creator update");
    }

}
