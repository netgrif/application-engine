package com.netgrif.workflow.startup

import com.netgrif.workflow.elastic.domain.ElasticCase
import com.netgrif.workflow.elastic.domain.ElasticTask
import groovy.util.logging.Slf4j
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate
import org.springframework.stereotype.Component

@Component
@Slf4j
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    @Value('${spring.data.elasticsearch.drop}')
    private boolean drop

    @Value('${spring.data.elasticsearch.cluster-name:elasticsearch}')
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
    private ElasticsearchRestTemplate template

    @Override
    void run(String... args) throws Exception {
        if (drop) {
            log.info("Dropping Elasticsearch database [${url}:${port}/${clusterName}]")
            template.deleteIndex(ElasticCase.class)
            template.deleteIndex(ElasticTask.class)
        }
        if (!template.indexExists(ElasticCase.class)) {
            log.info "Creating Elasticsearch case index [${caseIndex}]"
            template.createIndex(ElasticCase.class)
        } else {
            log.info "Elasticsearch case index exists [${caseIndex}]"
        }
        if (!template.indexExists(ElasticTask.class)) {
            log.info "Creating Elasticsearch task index [${taskIndex}]"
            template.createIndex(ElasticTask.class)
        } else {
            log.info "Elasticsearch task index exists [${taskIndex}]"
        }
        log.info("Updating Elasticsearch case mapping [${caseIndex}]")
        template.putMapping(ElasticCase.class)
        log.info("Updating Elasticsearch task mapping [${taskIndex}]")
        template.putMapping(ElasticTask.class)
    }
}