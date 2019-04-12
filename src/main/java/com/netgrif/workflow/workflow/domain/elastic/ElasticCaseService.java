package com.netgrif.workflow.workflow.domain.elastic;

import com.netgrif.workflow.workflow.domain.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ElasticCaseService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    @Autowired
    private ElasticCaseRepository repository;

    @Async
    @Override
    public void index(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);

        log.info("[" + useCase.getStringId() + "]: Case " + useCase.getTitle() + " indexed");
    }
}