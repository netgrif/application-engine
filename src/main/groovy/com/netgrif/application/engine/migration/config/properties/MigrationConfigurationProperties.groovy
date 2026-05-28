package com.netgrif.application.engine.migration.config.properties


import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "netgrif.migration")
class MigrationConfigurationProperties {

    private CaseMigrationProperties cases = new CaseMigrationProperties()

    private TaskMigrationProperties tasks = new TaskMigrationProperties()

    private PetriNetMigrationProperties petriNets = new PetriNetMigrationProperties()

    static class CaseMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

    static class TaskMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

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
