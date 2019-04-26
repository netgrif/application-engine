package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.web.ElasticSearchRequest;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

public interface IElasticCaseService {

    @Async
    void index(Case useCase);

    void indexNow(Case useCase);

    Page<ElasticCase> search(ElasticSearchRequest request, LoggedUser user, Pageable pageable);

    long count(ElasticSearchRequest request, LoggedUser user);
}
