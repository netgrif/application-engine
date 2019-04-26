package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.scheduling.annotation.Async;

public interface IElasticCaseService {

    @Async
    void index(Case useCase);

    void indexNow(Case useCase);
}
