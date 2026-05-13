package com.netgrif.application.engine.migration.helpers

import com.netgrif.application.engine.AsyncRunner
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties
import com.netgrif.application.engine.migration.config.properties.MigrationConfigurationProperties.PetriNetMigrationProperties
import com.netgrif.application.engine.petrinet.domain.PetriNet
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Slf4j
@Component
class PetriNetMigrationHelper extends AbstractMigrationHelper<PetriNet> {

    private PetriNetMigrationProperties petriNetMigrationProperties

    /**
     * Constructs a new PetriNetMigrationHelper with the specified MongoTemplate.
     *
     * @param mongoTemplate the {@link MongoTemplate} to use for interacting with MongoDB
     */
    PetriNetMigrationHelper(MongoTemplate mongoTemplate,
                            MigrationConfigurationProperties migrationConfigurationProperties) {
        super(PetriNet.class, mongoTemplate)
        this.petriNetMigrationProperties = migrationConfigurationProperties.petriNets
    }

    @Override
    int getPageSize() {
        return petriNetMigrationProperties.pageSize
    }

    @Override
    void prepareOperations(PetriNet document, Closure update, BulkOperations bulkOperations) {
        log.debug("Updating case with ID ${document.stringId}")
        log.trace("Updating case ${document.toString()}")
        update(document)
        bulkOperations.replaceOne(Query.query(Criteria.where("_id").is(document.getObjectId())), document)
    }


}
