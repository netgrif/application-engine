package com.netgrif.workflow.workflow.domain.elastic;

import com.netgrif.workflow.workflow.domain.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Component
@RepositoryEventHandler
public class CaseEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CaseEventHandler.class);

    @Autowired
    private ElasticsearchRepository repository;

    @Async
    @HandleAfterCreate
    @HandleAfterSave
    @PrePersist
    @PreUpdate
    public void afterCreate(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);
    }
}