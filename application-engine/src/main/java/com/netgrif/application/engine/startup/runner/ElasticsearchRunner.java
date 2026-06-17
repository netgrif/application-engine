package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase;
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticTask;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RunnerOrder(0)
@RequiredArgsConstructor
public class ElasticsearchRunner implements ApplicationEngineStartupRunner {

    private final DataConfigurationProperties.ElasticsearchProperties properties;

    private final IElasticIndexService template;

    private final String elasticPetriNetIndex;

    private final String elasticCaseIndex;

    private final String elasticTaskIndex;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (properties.getDrop()) {
            log.info("Dropping Elasticsearch database [{}:{}]", properties.getUrl(), properties.getPort());
            template.deleteIndex(ElasticPetriNet.class);
            template.deleteIndex(ElasticCase.class);
            template.deleteIndex(ElasticTask.class);
        }
        if (!template.indexExists(elasticPetriNetIndex)) {
            log.info("Creating Elasticsearch case index [{}]", elasticPetriNetIndex);
            template.createIndex(ElasticPetriNet.class);
        } else {
            log.info("Elasticsearch case index exists [{}]", elasticCaseIndex);
        }
        if (!template.indexExists(elasticCaseIndex)) {
            log.info("Creating Elasticsearch case index [{}]", elasticCaseIndex);
            template.createIndex(ElasticCase.class);
        } else {
            log.info("Elasticsearch case index exists [{}]", elasticCaseIndex);
        }
        if (!template.indexExists(elasticTaskIndex)) {
            log.info("Creating Elasticsearch task index [{}]", elasticTaskIndex);
            template.createIndex(ElasticTask.class);
        } else {
            log.info("Elasticsearch task index exists [{}]", elasticTaskIndex);
        }
        log.info("Updating Elasticsearch case mapping [{}]", elasticPetriNetIndex);
        template.putMapping(ElasticPetriNet.class);
        log.info("Updating Elasticsearch case mapping [{}]", elasticCaseIndex);
        template.putMapping(ElasticCase.class);
        log.info("Updating Elasticsearch task mapping [{}]", elasticTaskIndex);
        template.putMapping(ElasticTask.class);
    }
}
