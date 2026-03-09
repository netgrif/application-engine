package com.netgrif.application.engine.migration

import com.netgrif.application.engine.configuration.ApplicationShutdownProvider
import com.netgrif.application.engine.configuration.properties.MigrationProperties
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.AbstractOrderedCommandLineRunner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
abstract class MigrationOrderedCommandLineRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationOrderedCommandLineRunner)

    private String title = this.class.simpleName
    private boolean shutdownAfterFinish = false

    @Autowired
    private MigrationRepository repository

    @Autowired
    private MigrationProperties migrationProperties

    @Autowired
    private IPetriNetService service

    @Autowired
    private ApplicationShutdownProvider shutdownProvider

    @Override
    void run(String... strings) throws Exception {
        if (repository.existsByTitle(title)) {
            log.info("Migration ${title} was already applied")
            return
        }
        if (migrationProperties.getSkip().contains(title)) {
            log.info("Migration ${title} is skipped according to property nae.migration.skip")
            return
        }

        log.info("Applying migration ${title}")
        migrate()
        repository.save(new Migration(title))
        if (migrationProperties.isEvictCaches()) {
            log.info("Evicting all caches after migration")
            service.evictAllCaches()
        }
        log.info("Migration ${title} applied")
        if (shutdownAfterFinish || migrationProperties.isShutdownAfterMigration()) {
            sleep(100)
            shutdownProvider.shutdown(this.class)
        }
    }

    protected enableShutdownAfterFinish() {
        this.shutdownAfterFinish = true;
    }

    protected disableShutdownAfterFinish() {
        this.shutdownAfterFinish = false;
    }

    abstract void migrate()
}
