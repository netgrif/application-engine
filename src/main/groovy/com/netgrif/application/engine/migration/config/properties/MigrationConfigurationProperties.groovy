package com.netgrif.application.engine.migration.config.properties

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "netgrif.migration")
class MigrationConfigurationProperties {

    private CaseMigrationProperties cases = new CaseMigrationProperties()

    private TaskMigrationProperties tasks = new TaskMigrationProperties()

    private PetriNetMigrationProperties petriNets = new PetriNetMigrationProperties()

    @Data
    static class CaseMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

    @Data
    static class TaskMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

    @Data
    static class PetriNetMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

    CaseMigrationProperties getCases() {
        return cases
    }

    TaskMigrationProperties getTasks() {
        return tasks
    }

    PetriNetMigrationProperties getPetriNets() {
        return petriNets
    }
}
