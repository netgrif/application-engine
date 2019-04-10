package com.netgrif.workflow.migration


import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.AbstractOrderedCommandLineRunner
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class MigrationOrderedCommandLineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(MigrationOrderedCommandLineRunner)

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