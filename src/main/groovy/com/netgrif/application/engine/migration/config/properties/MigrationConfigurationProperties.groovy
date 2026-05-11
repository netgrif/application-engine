package com.netgrif.application.engine.migration.config.properties

import lombok.Data
import lombok.Getter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "netgrif.migration")
class MigrationConfigurationProperties {

    private CaseMigrationProperties cases = new CaseMigrationProperties()

    @Data
    static class CaseMigrationProperties {

        private int pageSize = 100

        int getPageSize() {
            return pageSize
        }
    }

    CaseMigrationProperties getCases() {
        return cases
    }
}
