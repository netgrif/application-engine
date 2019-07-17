package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.web.CaseSearchRequest;
import com.netgrif.workflow.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface IElasticCaseService {

    @Async
    void index(ElasticCase useCase);

    void indexNow(ElasticCase useCase);

    Page<Case> search(CaseSearchRequest request, LoggedUser user, Pageable pageable);

    long count(CaseSearchRequest request, LoggedUser user);

    Map<String, Float> fullTextFields();

    void remove(String caseId);
}