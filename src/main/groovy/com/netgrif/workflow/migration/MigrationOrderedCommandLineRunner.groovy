package com.netgrif.workflow.migration


import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.AbstractOrderedCommandLineRunner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class MigrationOrderedCommandLineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationOrderedCommandLineRunner)

    private String title = this.class.simpleName

    @Autowired
    private MigrationRepository repository

    @Autowired
    private IPetriNetService service

    @Override
    void run(String... strings) throws Exception {
        if (repository.existsByTitle(title)) {
            log.info("Migration ${title} was already applied")
            return
        }

        log.info("Applying migration ${title}")
        migrate()
        repository.save(new Migration(title))
        service.evictCache()
        log.info("Migration ${title} applied")
    }

    abstract void migrate()
}