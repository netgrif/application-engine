package com.netgrif.application.engine.migration

import com.netgrif.adapter.petrinet.service.PetriNetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
abstract class MigrationOrderedCommandLineRunner implements CommandLineRunner { // TODO rework migrations

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
        service.evictAllCaches()
        log.info("Migration ${title} applied")
    }

    abstract void migrate()
}
