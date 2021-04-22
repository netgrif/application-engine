package com.netgrif.workflow.export.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ExportConfiguration {

    @Value('${nae.export.mongo-page-size}')
    int mongoPageSize = 100

    @Value('${nae.export.elastic-page-size}')
    int elasticPageSize = 100

}
