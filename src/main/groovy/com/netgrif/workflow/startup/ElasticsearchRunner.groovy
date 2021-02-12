package com.netgrif.workflow.startup

import com.netgrif.workflow.elastic.domain.ElasticCase
import com.netgrif.workflow.elastic.domain.ElasticTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.stereotype.Component

@Component
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRunner)

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
    private ElasticsearchTemplate template

    @Override
    void run(String... args) throws Exception {
        if (drop) {
            log.info("Dropping Elasticsearch database [${url}:${port}/${clusterName}]")
            log.info "Creating Elasticsearch mapping"
            template.deleteIndex(CASE_INDEX)
            template.createIndex(CASE_INDEX)
            template.putMapping(CASE_INDEX, CASE_TYPE, CASE_MAPPING)

            template.deleteIndex(TASK_INDEX)
            template.createIndex(TASK_INDEX)
            template.putMapping(TASK_INDEX, TASK_TYPE, TASK_MAPPING)
        } else {
            if (!template.indexExists(CASE_INDEX)) {
                log.info "Creating Elasticsearch case mapping"
                template.createIndex(CASE_INDEX)
                template.putMapping(CASE_INDEX, CASE_TYPE, CASE_MAPPING)
            } else {
                log.info "Elasticsearch case mapping exists"
            }
            if (!template.indexExists(TASK_INDEX)) {
                log.info "Creating Elasticsearch task mapping"
                template.createIndex(TASK_INDEX)
                template.putMapping(TASK_INDEX, TASK_TYPE, TASK_MAPPING)
            } else {
                log.info "Elasticsearch task mapping exists"
            }
        }
        log.info("Updating Elasticsearch case mapping [${caseIndex}]")
        template.putMapping(ElasticCase.class)
        log.info("Updating Elasticsearch task mapping [${taskIndex}]")
        template.putMapping(ElasticTask.class)
    }
}