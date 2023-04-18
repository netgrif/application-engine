package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.properties.NaeQuartzProperties
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class QuartzSchedulerRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private NaeQuartzProperties quartzProperties

    @Autowired
    private Scheduler scheduler

    @Override
    void run(String... strings) throws Exception {
        if (quartzProperties.scheduler.run) {
            log.info("Starting Quartz scheduler")
            scheduler.start()
        }
    }
}