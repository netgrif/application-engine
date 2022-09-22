package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.properties.UriProperties
import com.netgrif.application.engine.elastic.domain.ElasticCase
import com.netgrif.application.engine.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService
import com.netgrif.application.engine.petrinet.domain.UriNode
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@Slf4j
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    @Value('${spring.data.elasticsearch.drop}')
    private boolean drop

    @Value('${spring.data.elasticsearch.cluster-name}')
    private String clusterName

    @Value('${spring.data.elasticsearch.url}')
    private String url

    @Value('${spring.data.elasticsearch.port}')
    private int port

    @Value('${spring.data.elasticsearch.index.case}')
    private String caseIndex

    @Value('${spring.data.elasticsearch.index.task}')
    private String taskIndex

    @Autowired
    private UriProperties uriProperties

    @Autowired
    private IElasticIndexService template

    @Override
    void run(String... args) throws Exception {
        if (drop) {
            log.info("Dropping Elasticsearch database [${url}:${port}/${clusterName}]")
            template.deleteIndex(ElasticCase.class)
            template.deleteIndex(ElasticTask.class)
            template.deleteIndex(UriNode.class)
        }
        if (!template.indexExists(caseIndex)) {
            log.info "Creating Elasticsearch case index [${caseIndex}]"
            template.createIndex(ElasticCase.class)
        } else {
            log.info "Elasticsearch case index exists [${caseIndex}]"
        }
        if (!template.indexExists(taskIndex)) {
            log.info "Creating Elasticsearch task index [${taskIndex}]"
            template.createIndex(ElasticTask.class)
        } else {
            log.info "Elasticsearch task index exists [${taskIndex}]"
        }
        if (!template.indexExists(uriProperties.index)) {
            log.info "Creating Elasticsearch uri index [${uriProperties.index}]"
            template.createIndex(UriNode.class)
        } else {
            log.info "Elasticsearch uri index exists [${uriProperties.index}]"
        }
        log.info("Updating Elasticsearch case mapping [${caseIndex}]")
        template.putMapping(ElasticCase.class)
        log.info("Updating Elasticsearch task mapping [${taskIndex}]")
        template.putMapping(ElasticTask.class)
        log.info("Updating Elasticsearch uri mapping [${uriProperties.index}]")
        template.putMapping(UriNode.class)
    }
}