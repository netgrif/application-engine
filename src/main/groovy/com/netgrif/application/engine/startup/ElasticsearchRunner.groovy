package com.netgrif.application.engine.startup

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties
import com.netgrif.application.engine.configuration.properties.UriProperties
import com.netgrif.application.engine.elastic.domain.ElasticCase
import com.netgrif.application.engine.elastic.domain.ElasticTask
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService
import com.netgrif.application.engine.petrinet.domain.UriNode
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties

    @Autowired
    private UriProperties uriProperties

    @Autowired
    private IElasticIndexService template

    @Override
    void run(String... args) throws Exception {
        if (elasticsearchProperties.drop) {
            log.info("Dropping Elasticsearch database [${elasticsearchProperties.url}:${elasticsearchProperties.port}/${elasticsearchProperties.clusterName}]")
            template.deleteIndex(ElasticCase.class)
            template.deleteIndex(ElasticTask.class)
            // TODO: release/7.0.0 6.2.5
            template.deleteIndex(UriNode.class)
        }
        if (!template.indexExists(elasticsearchProperties.index.get("case"))) {
            log.info "Creating Elasticsearch case index [${elasticsearchProperties.index.get("case")}]"
            template.createIndex(ElasticCase.class)
        } else {
            log.info "Elasticsearch case index exists [${elasticsearchProperties.index.get("case")}]"
        }
        if (!template.indexExists(elasticsearchProperties.index.get("task"))) {
            log.info "Creating Elasticsearch task index [${elasticsearchProperties.index.get("task")}]"
            template.createIndex(ElasticTask.class)
        } else {
            log.info "Elasticsearch task index exists [${elasticsearchProperties.index.get("task")}]"
        }
        if (!template.indexExists(uriProperties.index)) {
            log.info "Creating Elasticsearch uri index [${uriProperties.index}]"
            template.createIndex(UriNode.class)
        } else {
            log.info "Elasticsearch uri index exists [${uriProperties.index}]"
        }
        log.info("Updating Elasticsearch case mapping [${elasticsearchProperties.index.get("case")}]")
        template.putMapping(ElasticCase.class)
        log.info("Updating Elasticsearch task mapping [${elasticsearchProperties.index.get("task")}]")
        template.putMapping(ElasticTask.class)
        log.info("Updating Elasticsearch uri mapping [${uriProperties.index}]")
        template.putMapping(UriNode.class)
    }
}