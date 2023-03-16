package com.netgrif.application.engine.startup

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class QuartzSchedulerRunner extends AbstractOrderedCommandLineRunner {

    @Value('${quartz.scheduler.run:#{true}}')
    private boolean start

    @Autowired
    private Scheduler scheduler

    @Override
    void run(String... strings) throws Exception {
        if (start) {
            log.info("Starting Quartz scheduler")
            scheduler.start()
        }
    }
}