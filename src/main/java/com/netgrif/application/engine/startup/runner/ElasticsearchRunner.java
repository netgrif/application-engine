package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RunnerOrder(0)
@RequiredArgsConstructor
public class ElasticsearchRunner extends AbstractOrderedApplicationRunner {

    @Value("${spring.data.elasticsearch.drop}")
    private boolean drop;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    @Value("${spring.data.elasticsearch.url}")
    private String url;

    @Value("${spring.data.elasticsearch.port}")
    private int port;

    @Value("${spring.data.elasticsearch.index.petriNet}")
    private String petriNetIndex;

    @Value("${spring.data.elasticsearch.index.case}")
    private String caseIndex;

    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    private final IElasticIndexService template;

    @Override
    public void apply(ApplicationArguments args) throws Exception {
        if (drop) {
            log.info("Dropping Elasticsearch database [{}:{}/{}]", url, port, clusterName);
            template.deleteIndex(ElasticPetriNet.class);
            template.deleteIndex(ElasticCase.class);
            template.deleteIndex(ElasticTask.class);
        }
        if (!template.indexExists(petriNetIndex)) {
            log.info("Creating Elasticsearch case index [{}]", petriNetIndex);
            template.createIndex(ElasticPetriNet.class);
        } else {
            log.info("Elasticsearch case index exists [{}]", caseIndex);
        }
        if (!template.indexExists(caseIndex)) {
            log.info("Creating Elasticsearch case index [{}]", caseIndex);
            template.createIndex(ElasticCase.class);
        } else {
            log.info("Elasticsearch case index exists [{}]", caseIndex);
        }
        if (!template.indexExists(taskIndex)) {
            log.info("Creating Elasticsearch task index [{}]", taskIndex);
            template.createIndex(ElasticTask.class);
        } else {
            log.info("Elasticsearch task index exists [{}]", taskIndex);
        }
        log.info("Updating Elasticsearch case mapping [{}]", petriNetIndex);
        template.putMapping(ElasticPetriNet.class);
        log.info("Updating Elasticsearch case mapping [{}]", caseIndex);
        template.putMapping(ElasticCase.class);
        log.info("Updating Elasticsearch task mapping [{}]", taskIndex);
        template.putMapping(ElasticTask.class);
    }
}
