package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(190)
@RequiredArgsConstructor
public class QuartzSchedulerRunner implements ApplicationEngineStartupRunner {

    @Value("${quartz.scheduler.run:true}")
    private boolean start;

    private final Scheduler scheduler;

    @Override
    public void run(ApplicationArguments strings) throws Exception {
        if (start) {
            log.info("Starting Quartz scheduler");
            scheduler.start();
        }
    }

}
